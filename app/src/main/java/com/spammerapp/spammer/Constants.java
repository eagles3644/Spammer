package com.spammerapp.spammer;

import com.google.api.services.gmail.GmailScopes;

/**
 * Created by leona on 9/26/2015.
 */
public class Constants {

    //Scopes for sending email
    public static final String SCOPE_GMAIL_COMPOSE = GmailScopes.GMAIL_COMPOSE;
    public static final String SCOPE_GMAIL_MODIFY = GmailScopes.GMAIL_MODIFY;
    public static final String SCOPE_GMAIL_MAIL = GmailScopes.MAIL_GOOGLE_COM;

    //Shared Preferences
    public static final String PREF_ACCOUNT_NAME = "accountName";
    public static final String PREF_TOKEN = "token";
    public static final String PREF_24_HOUR = "24_hour";
    public static final String PREF_CONFIRM_ACCOUNT = "confirm_account";
    public static final String PREF_SENT_COUNT = "sent_count";
    public static final String PREF_HIST_ID = "hist_id";
    public static final String PREF_AD_FREE = "ad_free";
    public static final String PREF_DB_VERSION = "database_version_pref";
    public static final String PREF_ALREADY_INSTALLED = "already_installed";
    public static final String PREF_SEND_PROGRESS_NOTIF = "send_notif";
    public static final String PREF_SEND_COMPLETE_NOTIF = "sent_notif";

    //Ad Unit Id
    public static final String BANNER_AD_UNIT_ID = "ca-app-pub-6909068111618447/5679525813";
    public static final String FULL_SCREEN_AD_UNIT_ID = "ca-app-pub-6909068111618447/3755893418";

    //Permission Request IDs
    public static final int PERMIT_REQ_GET_ACCOUNTS = 4;

    //Notification IDs
    public static final int NOTIF_ID_SEND_PROGRESS = 36;
    public static final int NOTIF_ID_SEND_COMPLETE = 44;

    //Gmail Codes
    public static final int GMAIL_AUTHORIZATION_CODE = 1993;
    public static final int GMAIL_ACCOUNT_CODE = 1601;

    //Purchase IDs
    public static final String PURCHASE_AD_FREE = "";
}
