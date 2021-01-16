package ink.andromeda.dataflow.exception;

public class InterfaceInterceptedException extends IllegalStateException {

    public InterfaceInterceptedException(String url){
        super(String.format("url be intercepted: %s", url));
    }

    public InterfaceInterceptedException(String url, String message){
        super(String.format("url be intercepted: %s, message: %s", url, message));
    }

}
