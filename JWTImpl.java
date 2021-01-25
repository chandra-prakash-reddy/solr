

import io.jsonwebtoken.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.RandomStringUtils;

import javax.crypto.SecretKey;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;



public class JWTHandler implements FireshotsAuthentication {

    private static final String ALGORITHM_TYPE = "RSA";

    private static final String RSA_FILE_BEGIN_MARKER = "-----BEGIN RSA";

    private static final String RSA_FILE_END_MARKER = "-----END RSA";

    private static final String NOT_FOUND = " not found";


    private static final String JWT_ISSUER = "JWT_APPLICATION";

    private static final String AUTH_KEY = "auth_key";


    String publicKeypath;
    String privateKeyPath;
    String jwtTTL;
    String aesKeyPath;

    @Override
    public void init(HashMap authConfig, HashMap<String, Object> commonConfig) {
        publicKeypath = HashMapUtils.getString(commonConfig, "public_key");
        privateKeyPath = HashMapUtils.getString(commonConfig, "private_key");
        aesKeyPath = HashMapUtils.getString(commonConfig, "aes_key_path");
        try {
            jwtTTL = HashMapUtils.getString(authConfig, "JWT_TTL");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public HashMap getUserMap(HttpServletRequest httpServletRequest) throws Exception {
        return getParsedToken(httpServletRequest.getHeader("Authorization").replace(BEARER + " ", ""));
    }

    private HashMap<String, Object> getParsedToken(String token) throws Exception {
        Claims claims = null;
        try {
            claims = Jwts.parser()
                    .setSigningKey(RSAEncryptionUtils.getPublicKey(publicKeypath))
                    .parseClaimsJws(token).getBody();
        } catch (MalformedJwtException mje) {
            throw new UnAuthorizedServiceException("user is not authorized : MalformedJwt token");
        } catch (SignatureException sje) {
            throw new UnAuthorizedServiceException("user is not authorized : Signature invalid token");
        } catch (ExpiredJwtException eje) {
            throw new SessionExpired("user session expired");
        } catch (Exception e) {
            throw e;
        }
        HashMap<String, Object> map = new HashMap<>();
        for (String key : claims.keySet()) {
            if (!(key.equals("iat") || key.equals("exp"))) {
                map.put(key, claims.get(key));
            }
        }
        return map;
    }

    @Override
    public String generateToken(Map<String, Object> userdetails) throws Exception {
        try {
            long issueTime = new Date().getTime();
            userdetails.put(FireshotsCommon.SESSION_ID, generateSessionId(userdetails, issueTime));
            String token = Jwts.builder()
                    .setClaims(userdetails)
                    .setIssuer(JWT_ISSUER)
                    .signWith(SignatureAlgorithm.RS256, RSAEncryptionUtils.getPrivateKey(privateKeyPath))
                    .setIssuedAt(new Date(issueTime))
                    .setExpiration(new Date(System.currentTimeMillis() + Integer.parseInt(jwtTTL)))
                    .compact();
            return token;
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        }
    }


    @Override
    public Object exendToken(HashMap userDetails) throws Exception {
        try {
           
            String token = Jwts.builder().setClaims(userDetails)
                    .setIssuer(JWT_ISSUER)
                    .signWith(SignatureAlgorithm.RS256, RSAEncryptionUtils.getPrivateKey(privateKeyPath))
                    .setIssuedAt((Date) userDetails.get("iat"))
                    .setExpiration(new Date(System.currentTimeMillis() + Integer.parseInt(jwtTTL)))
                    .compact();
            return token;
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    public int generateSessionId(Map<String, Object> userDetailsMap, long issueTime) {
        int session_id;
 
        for (String k : userDetailsMap.keySet()) {
            userMap.put(k, userDetailsMap.get(k));
        }
        return -1;
    }
}

