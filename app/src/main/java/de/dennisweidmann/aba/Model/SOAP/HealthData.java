package de.dennisweidmann.aba.Model.SOAP;

import android.os.AsyncTask;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.ksoap2.HeaderProperty;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.MarshalDate;
import org.ksoap2.serialization.MarshalFloat;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpResponseException;
import org.ksoap2.transport.HttpsTransportSE;
import org.xmlpull.v1.XmlPullParserException;

public class HealthData {
    public static final String HOST = "try.ant-on.com";
    public static final String SOAP_URL = "/Wristband/HealthData";
    public static final String SOAP_ACTION = "https://" + HOST + SOAP_URL;
    public static final String METHOD_NAME_STATUS = "postStatus";
    public static final String METHOD_NAME_VALUES = "postValues";
    public static final String NAMESPACE = "https://try.ant-on.com/Wristband/";
    public static final byte[] SOAP_AUTH = "wristband:eR9yyX3ap26YQepI".getBytes();

    public HealthData() {}

    public void postStatus(String deviceId, int status) throws KeyManagementException, HttpResponseException, NoSuchAlgorithmException, IOException, XmlPullParserException {
        SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME_STATUS);
        request.addProperty("deviceId", deviceId);
        request.addProperty("status", status);
        SoapSerializationEnvelope soapEnvelope = getSoapSerializationEnvelope(request);
        getHttpsTransportSE().call(SOAP_ACTION, soapEnvelope, getHeader());
    }

    public void postValues(String deviceId, Vector<SensorValue> values) throws KeyManagementException, HttpResponseException, NoSuchAlgorithmException, IOException, XmlPullParserException {
        SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME_VALUES);
        request.addProperty("deviceId", deviceId);
        request.addProperty("values", values);
        SoapSerializationEnvelope soapEnvelope = getSoapSerializationEnvelope(request);
        new MarshalDate().register(soapEnvelope);
        new MarshalFloat().register(soapEnvelope);
        //getHttpsTransportSE().call(SOAP_ACTION, soapEnvelope, getHeader());
        new HealthDataAsyncTask(getHttpsTransportSE(), soapEnvelope, getHeader()).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
    }

    private List<HeaderProperty> getHeader() {
        List<HeaderProperty> headerList = new ArrayList<HeaderProperty>();
        headerList.add(new HeaderProperty("Authorization", "Basic "
                + org.kobjects.base64.Base64.encode(SOAP_AUTH)));
        return headerList;
    }

    private final SoapSerializationEnvelope getSoapSerializationEnvelope(
            SoapObject request) {
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                SoapEnvelope.VER11);
        envelope.dotNet = true;
        envelope.implicitTypes = true;
        envelope.setAddAdornments(false);
        envelope.setOutputSoapObject(request);
        return envelope;
    }

    private final HttpsTransportSE getHttpsTransportSE() throws IOException,
            KeyManagementException, NoSuchAlgorithmException {
        SSLConnection.allowAllSSL();
        HttpsTransportSE ht = new HttpsTransportSE(HOST, 443,
                SOAP_URL, 1000);
        ht.debug = true;
        return ht;
    }

    private class HealthDataAsyncTask extends AsyncTask <String, Float, String> {

        private HttpsTransportSE taskHTTPSTransportSE;
        private SoapEnvelope taskSoapEnvelope;
        private List<HeaderProperty> taskHeader;

        public HealthDataAsyncTask(HttpsTransportSE taskHTTPSTransportSE, SoapEnvelope taskSoapEnvelope, List<HeaderProperty> taskHeader) {
            this.taskHTTPSTransportSE = taskHTTPSTransportSE;
            this.taskSoapEnvelope = taskSoapEnvelope;
            this.taskHeader = taskHeader;
        }

        @Override
        protected String doInBackground(String... params) {
            if (taskHTTPSTransportSE == null || taskSoapEnvelope == null || taskHeader == null) {return null;}
            try {
                taskHTTPSTransportSE.call(HealthData.SOAP_ACTION, taskSoapEnvelope, taskHeader);
            } catch (IOException | XmlPullParserException e) {e.printStackTrace();}
            return null;
        }
    }

}
