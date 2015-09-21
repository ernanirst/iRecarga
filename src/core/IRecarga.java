/*
 * This class implements all API functions
 */
package core;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author ernanirst
 */
public abstract class IRecarga {

    /* User credentials (CHANGE THIS)*/
    private static final String user = "your@email.com";
    private static final String pass = "1234567890";
    
    /* API URLs */
    private static final String baseApiUrl = "https://www.irecarga.com.br/api/";
    private static final String validateLoginSenha = "https://www.irecarga.com.br/api/ValidateUser.ashx?action=validateLoginSenha";
    private static final String reloaderCompanyService = baseApiUrl + "ReloadCompany.ashx?action=reloaderCompanyService";
    private static final String reloaderValueCompany = baseApiUrl + "Company.ashx?action=reloaderValueCompany";
    private static final String reloadPhoneValue = baseApiUrl + "ReloadPhone.ashx?action=reloadPhoneValue";
    
    /* List of avalable providers */
    public static final String[] providersList = {"Claro", "Vivo", "Oi", "Tim", "Embratel", "Nextel"};

    /**
     * Make a recharge using your account. {@link IRecarga#user} and {@link IRecarga#pass} should specify
     * the account info.
     * @param ddd local DDD from the area the number is from.
     * @param provider provider of the specified number
     * @param number number of the phone
     * @param value value of the recharge in BRL
     * @return true if the recharge was successfull, else false
     */
    public static boolean makeRecharge(String ddd, String provider, String number, String value) {
        JSONObject json;
        JSONArray jarray;
        String token = getToken();
        if (token != null) {
            try {
                json = new JSONObject(postRequest(reloadPhoneValue, "&NN_DDD=" + ddd + "&NN_PHONENUMBER=" + number + "&NN_TOKEN=" + token + "&NN_COMPANY=" + provider + "&NN_VALUE=" + value + "&NN_OS=" + user));
                return json.getBoolean("status");
            } catch (JSONException ex) {
                Logger.getLogger(IRecarga.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }

        } else {
            return false;
        }
    }
    
    /**
     * Returns a access token for your account. {@link IRecarga#user} and {@link IRecarga#pass} should specify
     * the account info. Each token is reloaded every 30 minutes.
     * @return null if any error or a string with the token.
     */
    public static String getToken() {
        JSONObject json;
        try {
            json = new JSONObject(postRequest(validateLoginSenha, "&NN_LOGIN=" + user + "&NN_PASSWORD=" + pass));
        } catch (JSONException ex) {
            Logger.getLogger(IRecarga.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        try {
            if (json.getBoolean("status")) {
                return json.getString("token");
            } else {
                return null;
            }
        } catch (JSONException ex) {
            Logger.getLogger(IRecarga.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    /**
     * Returns the BRL balance on your account. {@link IRecarga#user} and {@link IRecarga#pass} should specify
     * the account info.
     * @return null if any error or number separated by comma.
     */
    public static String getBalance() {
        JSONObject json;
        try {
            json = new JSONObject(postRequest(validateLoginSenha, "&NN_LOGIN=" + user + "&NN_PASSWORD=" + pass));
        } catch (JSONException ex) {
            Logger.getLogger(IRecarga.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        try {
            if (json.getBoolean("status")) {
                return json.getString("saldoAtual");
            } else {
                return null;
            }
        } catch (JSONException ex) {
            Logger.getLogger(IRecarga.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    /**
     * Returns all possibles values to a specific DDD and provider. {@link IRecarga#user} and {@link IRecarga#pass} should specify
     * the account info.
     * @param ddd local DDD from the area the number is from.
     * @param provider provider of the specified number
     * @return null if error, else a string with all values separated by " ", numbers use comma not point.
     */
    public static String getValues(String ddd, String provider) {
        JSONObject json;
        JSONArray jarray;
        String token = getToken();
        String ret = "";
        if (token != null) {
            try {
                json = new JSONObject(postRequest(reloaderValueCompany, "&NN_DDD=" + ddd + "&NN_TOKEN=" + token + "&NN_COMPANY=" + provider));
                jarray = json.getJSONArray("value");
                for (int i = 0; i < jarray.length(); i++) {
                    ret += jarray.getString(i);
                    if (i != jarray.length() - 1) {
                        ret += " ";
                    }
                }
            } catch (JSONException ex) {
                Logger.getLogger(IRecarga.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
            return ret;
        } else {
            return null;
        }
    }

    /**
     * Returns all possibles providers to a specific DDD. {@link IRecarga#user} and {@link IRecarga#pass} should specify
     * the account info.
     * @param ddd local DDD from the area the number is from.
     * @return null if error, else a string with all providers separated by " ".
     */
    public static String getProviders(String ddd) {
        JSONObject json;
        JSONArray jarray;
        String ret = "";
        String token = getToken();
        if (token != null) {
            try {
                json = new JSONObject(postRequest(reloaderCompanyService, "&NN_DDD=" + ddd + "&NN_TOKEN=" + token));
                jarray = json.getJSONArray("operadoras");
                if(json.getJSONArray("operadoras")==null) System.out.println("whaaat nigga");
                for (int i = 0; i < jarray.length(); i++) {
                    ret += jarray.getString(i);
                    if (i != jarray.length() - 1) {
                        ret += " ";
                    }
                }
            } catch (JSONException ex) {
                Logger.getLogger(IRecarga.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }

            return ret;

        } else {
            return token;
        }
    }

    /**
     * Returns the text in the url.
     * @param targetURL url where the text is.
     * @param postParams parameters to obtain the text.
     * @return the text in the targetURL.
     */
    public static String postRequest(String targetURL, String postParams) {
        URL url;
        HttpURLConnection connection = null;
        try {
            // Create connection
            url = new URL(targetURL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");

            connection.setRequestProperty("Content-Length", ""
                    + Integer.toString(postParams.getBytes().length));
            connection.setRequestProperty("Content-Language", "en-US");

            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            // Send request
            DataOutputStream wr = new DataOutputStream(connection
                    .getOutputStream());
            wr.writeBytes(postParams);
            wr.flush();
            wr.close();

            // Get Response
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            String line;
            StringBuffer response = new StringBuffer();
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();
            return response.toString();

        } catch (Exception e) {

            e.printStackTrace();
            return null;

        } finally {

            if (connection != null) {
                connection.disconnect();
            }
        }
    }

}

