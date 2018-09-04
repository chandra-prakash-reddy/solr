


import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import java.io.IOException;
import java.util.*;


public class SolrUtil {

    private Logger LOGGER;
    private static final String DATA_KEY = "data";
    private static final int BATCH_SIZE_TO_INSERT = 10000;
    private String solrHost;

    /**
     * This method is used for initializing the host
     * @param solrHost
     */
    public void init(String solrHost){
        this.solrHost = "http://"+solrHost+":8983/solr";
    }

    /**
     * This method is used for searching Documents in solr
     * @param solrQuery determines the input solrQuery
     * @param collection determines the collection name
     * @return Map </String,Object> as query response
     * @throws IOException
     */
    public Map<String,Object> searchDocuments(SolrQuery solrQuery,String collection) throws IOException {
        Map<String,Object> result=new HashMap<>();
        SolrClient solrClient=null;
        try {
            solrClient=new HttpSolrClient.Builder().withBaseSolrUrl(solrHost).build();
            QueryResponse response=solrClient.query(collection,solrQuery);
            solrQuery.setRows((int) response.getResults().getNumFound());
            response=solrClient.query(collection,solrQuery);
            result.put(DATA_KEY, new LinkedList<>(response.getResults()));
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Unable to connect to solr :" + e.getMessage());
        } finally {
            close(solrClient);
        }
        return result;
    }

    /**
     * This method is used for inserting Documents into solr
     * @param dataMapList Documents List
     * @param collection solr collection name
     * @return int of no of documents inserted
     * @throws IOException
     */
    public int insertSolrDocuments(List<Map<String, Object>> dataMapList,String collection) throws  IOException {
        List<SolrInputDocument> solrInputDocumentList = prepareSolrInputDocumentList(dataMapList);
        SolrClient solrClient = null;
        String hostUrl=solrHost+"/"+collection;
        try {
            HttpClient httpClient=HttpClients.custom().build();
            solrClient =new HttpSolrClient.Builder().withBaseSolrUrl(hostUrl).withHttpClient(httpClient).build();
            solrClient.commit();
            int batch = 0;
            for (int i = 0; i <= solrInputDocumentList.size() + BATCH_SIZE_TO_INSERT; i = i + BATCH_SIZE_TO_INSERT) {
                if (solrInputDocumentList.size() < i) {
                    solrClient.add(solrInputDocumentList.subList(batch, solrInputDocumentList.size()));
                    solrClient.commit();
                    LOGGER.info("Inserted rows from :- [" + batch + "] to [" + solrInputDocumentList.size() + "]");
                    break;
                } else if (i != 0) {
                    solrClient.add(solrInputDocumentList.subList(batch, i));
                    solrClient.commit();
                    LOGGER.info("Inserted rows from :- [" + batch + "] to [" + i + "]");
                    batch = i;
                }
            }
            solrClient.commit();
            return solrInputDocumentList.size();
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.info("failed to insert : " + e.getMessage());
        } finally {
          close(solrClient);
        }
        return -1;
    }

    /**
     * This method is used for deleting the documents
     * @param query determines the query for deleting the documents
     * @param collection determines the collection
     * @return  1 if deleted successfully
     * @throws Exception when issue with deleting documents
     */
    public boolean deleteSolrDocuments(String query,String collection) throws Exception {
        String hostUrl=solrHost+"/"+collection;
        SolrClient solrClient = null;
        try {
            solrClient = new HttpSolrClient.Builder().withBaseSolrUrl(hostUrl).build();
            solrClient.commit();
            solrClient.deleteByQuery(query);
            solrClient.commit();
            LOGGER.info("Documents deleted successfully");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.info("Failed to delete documents");
        } finally {
           close(solrClient);
        }
        return false;

    }

    /**
     * This method used for preparing the Solr Documents
     * @param dataList determines the dataList to be converted to solrDocuments
     * @return List<SolrInputDocument> converted SolrDocumentsList
     */
    public List<SolrInputDocument> prepareSolrInputDocumentList(List<Map<String, Object>> dataList) {
        List<SolrInputDocument> solrInputDocumentList = new ArrayList<>();
        Set<String> fields = dataList.get(0).keySet();
        for (Map<String, Object> dataMap : dataList) {
            SolrInputDocument solrInputDocument = new SolrInputDocument();
            for (String field : fields) {
                solrInputDocument.addField(field, dataMap.get(field));
            }
            solrInputDocumentList.add(solrInputDocument);
        }
        return solrInputDocumentList;
    }


    /**
     * This method used for closing the Solr Client
     * @param client
     * @throws IOException
     */
    private void close(SolrClient client) throws IOException {
        if (client != null) {
            client.close();
        }
    }


}
