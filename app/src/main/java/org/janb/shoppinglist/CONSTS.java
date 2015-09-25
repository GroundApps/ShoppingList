package org.janb.shoppinglist;

public class CONSTS {

    public static final int MINIMUM_REQUIRED_BACKEND_VERSION = 1;

    public static final int TAG_LIST = 1;
    public static final int TAG_FAVORITES = 2;
    public static final int TAG_SETTINGS = 3;
    public static final int TAG_ABOUT= 4;

    public static final int API_SUCCESS_LIST = 1000;
    public static final int API_SUCCESS_LIST_EMPTY = 1001;
    public static final int API_SUCCESS_UPDATE = 1002;
    public static final int API_SUCCESS_IMPORTANT = 1003;
    public static final int API_SUCCESS_DELETE = 1004;
    public static final int API_SUCCESS_SAVE = 1005;
    public static final int API_SUCCESS_CLEAR = 1006;

    //Errors
    public static final int APP_ERROR_CONNECT = 2000;
    public static final int APP_ERROR_HOST_NOT_FOUND = 2001;
    public static final int APP_ERROR_URL_EXCEPTION = 2002;
    public static final int APP_ERROR_IO = 2003;
    public static final int APP_ERROR_CONFIG_NO_HOST = 2004;
    public static final int APP_ERROR_UNKNOWN = 2005;
    public static final int APP_ERROR_RESPONSE = 2006;
    public static final int APP_BACKEND_VERSION = 2007;


    public static final int API_ERROR_SERVER = 5000;
    public static final int API_ERROR_404 = 5001;
    public static final int API_ERROR_403 = 5002;
    public static final int API_ERROR_MISSING_FUNCTION = 5003;
    public static final int API_ERROR_NO_DATABASE = 5004;
    public static final int API_ERROR_CONFIG = 5005;
    public static final int API_ERROR_NO_VERSION = 5006;

    public static final int API_ERROR_UPDATE_ = 5007;
    public static final int API_ERROR_FAVORITE = 5008;
    public static final int API_ERROR_DELETE = 5009;
    public static final int API_ERROR_SAVE = 5010;
}
