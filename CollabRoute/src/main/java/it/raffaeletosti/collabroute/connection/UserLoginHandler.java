package it.raffaeletosti.collabroute.connection;


import android.content.DialogInterface;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

import it.raffaeletosti.collabroute.LoginActivity;
import it.raffaeletosti.collabroute.model.UserHandler;

/**
 * Created by raffaele on 12/03/14.
 */
public class UserLoginHandler extends ConnectionHandler {



    public static UserHandler user;
    private String eMail;
    private String password;
    private JSONObject error;
    private int code;

    public UserLoginHandler(LoginActivity activity, UserHandler user) {
        super(activity);
        UserLoginHandler.user = user;
        error = new JSONObject();
    }

    public UserLoginHandler(LoginActivity activity, String eMail){
        super(activity);
        this.eMail = eMail;
        error = new JSONObject();
    }

    public UserLoginHandler(LoginActivity activity, int code, String password, String eMail){
        super(activity);
        this.code = code;
        this.password = password;
        this.eMail = eMail;
        error = new JSONObject();
    }


    @Override
    protected void onPreExecute() {
        dialog.setMessage("Sending request, hold on please");
        dialog.show();
    }

    @Override
    protected void onPostExecute(Object result) {
        if(dialog.isShowing()){
            dialog.dismiss();
        }
        try {
            JSONObject jsonResult = (JSONObject) result;
            String responseType = jsonResult.getString("type");
            if(responseType.equals("login")){
                ((LoginActivity)activity).checkCredentials(jsonResult);
                return;
            }
            if(responseType.equals("recovery_request")){
                ((LoginActivity)activity).handleResponseRecoveryRequest(jsonResult);
                return;
            }
            if(responseType.equals("password_recovery")){
                ((LoginActivity)activity).handleRecoveryPasswordResponse(jsonResult);
                return;
            }
            ((LoginActivity)activity).confirmationResponse(jsonResult);
        } catch (JSONException e) {
            System.err.println(e);
        }
    }
    @Override
    protected JSONObject doInBackground(String... params) {
        try {
            if (params[0].equals("login"))
                return doLoginData();
            if(params[0].equals("recovery"))
                return sendPasswordRecoveryRequest();
            if(params[0].equals("sendPass"))
                return sendNewPassAndCode();
            return confirmUser();
        } catch (JSONException e) {
            System.err.println(e);
        }
        return null;
    }

    JSONObject doLoginData() throws JSONException {
        try {

            String urlString = "https://" + serverUrl + ":" + serverPort + "/auth/" + user.getEMail() + "/" + user.getPassword();
            URL url = new URL(urlString);

            /** Create all-trusting host name verifier
             * to avoid the following :
             * java.security.cert.CertificateException: No name matching
             * This is because Java by default verifies that the certificate CN (Common Name) is
             * the same as host name in the URL. If they are not, the web service client fails.
             **/

            HostnameVerifier allowEveryHost = new HostnameVerifier() {

                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            };

            HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setConnectTimeout(3000);
            urlConnection.setSSLSocketFactory(context.getSocketFactory());
            urlConnection.setHostnameVerifier(allowEveryHost);
            InputStream in = urlConnection.getInputStream();
            //System.err.println(inputToString(in)); debug
            String jsonToString = inputToString(in);
            in.close();
            return new JSONObject(jsonToString);
        } catch (SocketTimeoutException e) {
            System.err.println(e);
            error.put("result", "CONN_TIMEDOUT").put("type" , "login");
            return error;
        } catch (ConnectException e) {
            System.err.println(e);
            error.put("result", "CONN_REFUSED").put("type" , "login");
            return error;
        } catch (MalformedURLException e) {
            System.err.println(e);
            error.put("result", "CONN_BAD_URL").put("type" , "login");
            return error;
        } catch (IOException e) {
            System.err.println(e);
            error.put("result", "CONN_GENERIC_IO_ERROR").put("type" , "login");
            return error;
        } catch (IllegalArgumentException e) {
            System.err.println(e);
            error.put("result", "CONN_GENERIC_ERROR").put("type" , "login");
            return error;
        } catch (Exception e) {
            System.err.println(e);
            error.put("result", "CONN_GENERIC_ERROR").put("type" , "login");
            return error;
        }
    }

    protected JSONObject confirmUser() throws JSONException{
        try {
            String urlString = "https://" + serverUrl + ":" + serverPort + "/confirm/user/";
            URL url = new URL(urlString);

            /** Create all-trusting host name verifier
             * to avoid the following :
             * java.security.cert.CertificateException: No name matching
             * This is because Java by default verifies that the certificate CN (Common Name) is
             * the same as host name in the URL. If they are not, the web service client fails.
             **/

            HostnameVerifier allowEveryHost = new HostnameVerifier() {

                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            };

            HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setConnectTimeout(3000);
            urlConnection.setSSLSocketFactory(context.getSocketFactory());
            urlConnection.setHostnameVerifier(allowEveryHost);
            urlConnection.setRequestMethod("PUT");
            String urlParam = "mail=" + user.getEMail();
            DataOutputStream printout = new DataOutputStream(urlConnection.getOutputStream());
            printout.writeBytes(urlParam);
            printout.flush();
            printout.close();
            InputStream in = urlConnection.getInputStream();
            String jsonToString = inputToString(in);
            in.close();
            return new JSONObject(jsonToString);
        } catch (SocketTimeoutException e) {
            System.err.println(e);
            error.put("type", "confirm").put("result", "CONN_TIMEDOUT");
            return error;
        } catch (ConnectException e) {
            System.err.println(e);
            error.put("type", "confirm").put("result", "CONN_REFUSED");
            return error;
        } catch (MalformedURLException e) {
            System.err.println(e);
            error.put("type", "confirm").put("result", "CONN_BAD_URL");
            return error;
        } catch (IOException e) {
            System.err.println(e);
            error.put("type", "confirm").put("result", "CONN_GENERIC_IO_ERROR");
            return error;
        } catch (IllegalArgumentException e) {
            System.err.println(e);
            error.put("type", "confirm").put("result", "CONN_GENERIC_ERROR");
            return error;
        } catch (Exception e) {
            System.err.println(e);
            error.put("type", "confirm").put("result", "CONN_GENERIC_ERROR");
            return error;
        }
    }

    JSONObject sendPasswordRecoveryRequest() throws JSONException {
        try {

            String urlString = "https://" + serverUrl + ":" + serverPort + "/recovery/" + eMail;
            URL url = new URL(urlString);

            /** Create all-trusting host name verifier
             * to avoid the following :
             * java.security.cert.CertificateException: No name matching
             * This is because Java by default verifies that the certificate CN (Common Name) is
             * the same as host name in the URL. If they are not, the web service client fails.
             **/

            HostnameVerifier allowEveryHost = new HostnameVerifier() {

                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            };

            HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setConnectTimeout(3000);
            urlConnection.setSSLSocketFactory(context.getSocketFactory());
            urlConnection.setHostnameVerifier(allowEveryHost);
            urlConnection.setRequestMethod("GET");
            InputStream in = urlConnection.getInputStream();
            //System.err.println(inputToString(in)); debug
            String jsonToString = inputToString(in);
            in.close();
            return new JSONObject(jsonToString);
        } catch (SocketTimeoutException e) {
            System.err.println(e);
            error.put("result", "CONN_TIMEDOUT").put("type" , "recovery_request");
            return error;
        } catch (ConnectException e) {
            System.err.println(e);
            error.put("result", "CONN_REFUSED").put("type" , "recovery_request");
            return error;
        } catch (MalformedURLException e) {
            System.err.println(e);
            error.put("result", "CONN_BAD_URL").put("type" , "recovery_request");
            return error;
        } catch (IOException e) {
            System.err.println(e);
            error.put("result", "CONN_GENERIC_IO_ERROR").put("type" , "recovery_request");
            return error;
        } catch (IllegalArgumentException e) {
            System.err.println(e);
            error.put("result", "CONN_GENERIC_ERROR").put("type" , "recovery_request");
            return error;
        } catch (Exception e) {
            System.err.println(e);
            error.put("result", "CONN_GENERIC_ERROR").put("type" , "recovery_request");
            return error;
        }
    }

    protected JSONObject sendNewPassAndCode() throws JSONException{
    try {
        String urlString = "https://" + serverUrl + ":" + serverPort + "/recovery/update";
        URL url = new URL(urlString);

        /** Create all-trusting host name verifier
         * to avoid the following :
         * java.security.cert.CertificateException: No name matching
         * This is because Java by default verifies that the certificate CN (Common Name) is
         * the same as host name in the URL. If they are not, the web service client fails.
         **/

        HostnameVerifier allowEveryHost = new HostnameVerifier() {

            @Override
                public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };

        HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
        urlConnection.setConnectTimeout(3000);
        urlConnection.setSSLSocketFactory(context.getSocketFactory());
        urlConnection.setHostnameVerifier(allowEveryHost);
        urlConnection.setRequestMethod("PUT");
        urlConnection.setRequestProperty("Content-Type" , "application/json");
        DataOutputStream printout = new DataOutputStream(urlConnection.getOutputStream());
        String request = new JSONObject().put("pass" , password).put("code", String.valueOf(code)).put("mail" , eMail).toString();
        //System.err.println(request);
        printout.writeBytes(request);
        printout.flush();
        printout.close();
        InputStream in = urlConnection.getInputStream();
        String jsonToString = inputToString(in);
        in.close();
        return new JSONObject(jsonToString);
    } catch (SocketTimeoutException e) {
        System.err.println(e);
        error.put("type", "password_recovery").put("result", "CONN_TIMEDOUT");
        return error;
    } catch (ConnectException e) {
        System.err.println(e);
        error.put("type", "password_recovery").put("result", "CONN_REFUSED");
        return error;
    } catch (MalformedURLException e) {
        System.err.println(e);
        error.put("type", "password_recovery").put("result", "CONN_BAD_URL");
            return error;
    } catch (IOException e) {
        System.err.println(e);
        error.put("type", "password_recovery").put("result", "CONN_GENERIC_IO_ERROR");
        return error;
    } catch (IllegalArgumentException e) {
        System.err.println(e);
        error.put("type", "password_recovery").put("result", "CONN_GENERIC_ERROR");
        return error;
    } catch (Exception e) {
        System.err.println(e);
        error.put("type", "password_recovery").put("result", "CONN_GENERIC_ERROR");
        return error;
    }
}
}