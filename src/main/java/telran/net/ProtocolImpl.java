package telran.net;

public class ProtocolImpl implements Protocol {

    @Override
    public Response getResponse(Request request) {
        ResponseCode responseCode = ResponseCode.OK;
        String responseData = "RequestType: " + request.requestType() + ", requestData: " + request.requestData();

        return new Response(responseCode, responseData);
    }

}
