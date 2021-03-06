/*
 * Copyright 2013-2016 microG Project Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.gms.gcm;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;

import com.google.android.gms.iid.InstanceID;

import org.microg.gms.common.PublicApi;
import org.microg.gms.gcm.GcmConstants;

import java.io.IOException;

import static org.microg.gms.common.Constants.GMS_PACKAGE_NAME;
import static org.microg.gms.gcm.GcmConstants.ACTION_C2DM_RECEIVE;
import static org.microg.gms.gcm.GcmConstants.ACTION_GCM_SEND;
import static org.microg.gms.gcm.GcmConstants.EXTRA_APP;
import static org.microg.gms.gcm.GcmConstants.EXTRA_DELAY;
import static org.microg.gms.gcm.GcmConstants.EXTRA_MESSAGE_ID;
import static org.microg.gms.gcm.GcmConstants.EXTRA_MESSAGE_TYPE;
import static org.microg.gms.gcm.GcmConstants.EXTRA_SENDER_LEGACY;
import static org.microg.gms.gcm.GcmConstants.EXTRA_SEND_TO;
import static org.microg.gms.gcm.GcmConstants.EXTRA_TTL;
import static org.microg.gms.gcm.GcmConstants.PERMISSION_GTALK;

/**
 * GoogleCloudMessaging (GCM) enables apps to communicate with their app servers
 * using simple messages.
 * <p/>
 * To send or receive messages, the app must get a
 * registrationToken from {@link com.google.android.gms.iid.InstanceID#getToken(java.lang.String, java.lang.String)}, which authorizes an
 * app server to send messages to an app instance. Pass sender ID and
 * {@link com.google.android.gms.gcm.GoogleCloudMessaging#INSTANCE_ID_SCOPE} as parameters to the method.
 * A sender ID is a project number acquired from the API console, as described in
 * <a href="http://developer.android.com/google/gcm/gs.html">Getting Started</a>.
 * <p/>
 * In order to receive GCM messages, declare {@link com.google.android.gms.gcm.GcmReceiver}
 * and an implementation of {@link com.google.android.gms.gcm.GcmListenerService} in the app manifest.
 * {@link com.google.android.gms.gcm.GcmReceiver} will pass the incoming messages to the implementation
 * of {@link com.google.android.gms.gcm.GcmListenerService}. To process messages, override base class
 * methods to handle any events required by the application.
 * <p/>
 * Client apps can send upstream messages back to the app server using the XMPP-based
 * <a href="http://developer.android.com/google/gcm/ccs.html">Cloud Connection Server</a>,
 * For example:
 * <p/>
 * gcm.send(SENDER_ID + "@gcm.googleapis.com", id, data);</pre>
 * See <a href="https://developers.google.com/cloud-messaging/android/client">Implementing GCM Client on Android</a> for more details.
 */
@PublicApi
public class GoogleCloudMessaging {
    /**
     * The GCM {@link com.google.android.gms.gcm.GoogleCloudMessaging#register(java.lang.String...)} and {@link com.google.android.gms.gcm.GoogleCloudMessaging#unregister()} methods are
     * blocking. Blocking methods must not be called on the main thread.
     */
    public static final String ERROR_MAIN_THREAD = "MAIN_THREAD";

    /**
     * The device can't read the response, or there was a 500/503 from the
     * server that can be retried later. The application should use exponential
     * back off and retry.
     */
    public static final String ERROR_SERVICE_NOT_AVAILABLE = GcmConstants.ERROR_SERVICE_NOT_AVAILABLE;

    /**
     * Specifies scope used in obtaining GCM registrationToken when calling
     * {@link com.google.android.gms.iid.InstanceID#getToken(java.lang.String, java.lang.String)}
     */
    public static final String INSTANCE_ID_SCOPE = GcmConstants.INSTANCE_ID_SCOPE_GCM;

    /**
     * Returned by {@link com.google.android.gms.gcm.GoogleCloudMessaging#getMessageType(android.content.Intent)} to indicate that the server deleted
     * some pending messages because they exceeded the storage limits. The
     * application should contact the server to retrieve the discarded messages.
     *
     * @deprecated Instead implement {@link com.google.android.gms.gcm.GcmListenerService#onDeletedMessages()}
     */
    @Deprecated
    public static final String MESSAGE_TYPE_DELETED = GcmConstants.MESSAGE_TYPE_DELETED_MESSAGE;

    /**
     * Returned by {@link com.google.android.gms.gcm.GoogleCloudMessaging#getMessageType(android.content.Intent)} to indicate a regular message.
     *
     * @deprecated Instead implement {@link com.google.android.gms.gcm.GcmListenerService#onMessageReceived(java.lang.String, android.os.Bundle)}
     */
    @Deprecated
    public static final String MESSAGE_TYPE_MESSAGE = GcmConstants.MESSAGE_TYPE_GCM;

    /**
     * Returned by {@link com.google.android.gms.gcm.GoogleCloudMessaging#getMessageType(android.content.Intent)} to indicate a send error.
     * The intent includes the message ID of the message and an error code.
     *
     * @deprecated Instead implement {@link com.google.android.gms.gcm.GcmListenerService#onSendError(java.lang.String, java.lang.String)}
     */
    @Deprecated
    public static final String MESSAGE_TYPE_SEND_ERROR = GcmConstants.MESSAGE_TYPE_SEND_ERROR;

    /**
     * Returned by {@link com.google.android.gms.gcm.GoogleCloudMessaging#getMessageType(android.content.Intent)} to indicate a sent message has been received by the GCM
     * server. The intent includes the message ID of the message.
     *
     * @deprecated Instead implement {@link com.google.android.gms.gcm.GcmListenerService#onMessageSent(java.lang.String)}
     */
    @Deprecated
    public static final String MESSAGE_TYPE_SEND_EVENT = GcmConstants.MESSAGE_TYPE_SEND_EVENT;

    private static GoogleCloudMessaging INSTANCE;
    /**
     * Due to it's nature of being a monitored reference, Intents can be used to authenticate a package source.
     */
    private PendingIntent selfAuthIntent;
    private Context context;

    public GoogleCloudMessaging() {
        throw new UnsupportedOperationException();
    }

    /**
     * Must be called when your application is done using GCM, to release
     * internal resources.
     */
    public void close() {
        throw new UnsupportedOperationException();
    }

    private PendingIntent getSelfAuthIntent() {
        if (selfAuthIntent == null) {
            Intent intent = new Intent();
            intent.setPackage("com.google.example.invalidpackage");
            selfAuthIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        }
        return selfAuthIntent;
    }

    /**
     * Return the singleton instance of GCM.
     */
    public static GoogleCloudMessaging getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new GoogleCloudMessaging();
            INSTANCE.context = context.getApplicationContext();
        }
        return INSTANCE;
    }

    /**
     * Return the message type from an intent passed into a client app's broadcast receiver. There
     * are two general categories of messages passed from the server: regular GCM messages,
     * and special GCM status messages.
     * <p/>
     * The possible types are:
     * {@link #MESSAGE_TYPE_MESSAGE}, {@link #MESSAGE_TYPE_DELETED}, {@link #MESSAGE_TYPE_SEND_EVENT} and {@link #MESSAGE_TYPE_SEND_ERROR}
     * <p/>
     * You can use this method to filter based on message type. Since it is likely that GCM will
     * be extended in the future with new message types, just ignore any message types you're not
     * interested in, or that you don't recognize.
     *
     * @return The message type or null if the intent is not a GCM intent
     */
    public String getMessageType(Intent intent) throws IOException {
        if (intent == null || !ACTION_C2DM_RECEIVE.equals(intent.getAction())) return null;
        if (!intent.hasExtra(EXTRA_MESSAGE_TYPE)) return MESSAGE_TYPE_MESSAGE;
        return intent.getStringExtra(EXTRA_MESSAGE_TYPE);
    }

    /**
     * Register the application for GCM and return the registration ID. You must call this once,
     * when your application is installed, and send the returned registration ID to the server.
     * <p/>
     * Repeated calls to this method will return the original registration ID.
     * <p/>
     * If you want to modify the list of senders, you must call <code>unregister()</code> first.
     * <p/>
     * Most applications use a single sender ID. You may use multiple senders if different
     * servers may send messages to the app or for testing.</p>
     *
     * @param senderIds list of project numbers or Google accounts identifying who is allowed to
     *                  send messages to this application.
     * @return registration id
     * @throws IOException
     * @deprecated Instead, for GCM registration, use
     * {@link com.google.android.gms.iid.InstanceID#getToken(java.lang.String, java.lang.String)}.
     * Set authorizedEntity to a sender ID and scope to {@link com.google.android.gms.gcm.GoogleCloudMessaging#INSTANCE_ID_SCOPE}.
     */
    @Deprecated
    public String register(String... senderIds) throws IOException {
        if (Looper.getMainLooper() == Looper.myLooper()) throw new IOException(ERROR_MAIN_THREAD);
        if (senderIds == null || senderIds.length == 0) throw new IllegalArgumentException("No sender ids");
        StringBuilder sb = new StringBuilder(senderIds[0]);
        for (int i = 1; i < senderIds.length; i++) {
            sb.append(',').append(senderIds[i]);
        }
        // This seems to be a legacy variant
        // TODO: Implement latest version
        Bundle extras = new Bundle();
        extras.putString(EXTRA_SENDER_LEGACY, sb.toString());
        return InstanceID.getInstance(context).getToken(sb.toString(), INSTANCE_ID_SCOPE, extras);
    }

    /**
     * Send an upstream ("device to cloud") message. You can only use the upstream feature
     * if your GCM implementation uses the XMPP-based
     * <a href="http://developer.android.com/google/gcm/ccs.html">Cloud Connection Server</a>.
     * <p/>
     * The current limits for max storage time and number of outstanding messages per
     * application are documented in the
     * <a href="http://developer.android.com/google/gcm/index.html">GCM Developers Guide</a>.</p>
     *
     * @param to         string identifying the receiver of the message in the format of
     *                   <code>SENDER_ID@gcm.googleapis.com</code>. The <code>SENDER_ID</code> should be one of the sender
     *                   IDs used when calling  {@link com.google.android.gms.iid.InstanceID#getToken(java.lang.String, java.lang.String)}
     * @param msgId      ID of the message. This is generated by the application. It must be
     *                   unique for each message. This allows error callbacks and debugging.
     * @param timeToLive If 0, we'll attempt to send immediately and return an
     *                   error if we're not connected. Otherwise, the message will be queued.
     *                   As for server-side messages, we don't return an error if the message has been
     *                   dropped because of TTL—this can happen on the server side, and it would require
     *                   extra communication.
     * @param data       key/value pairs to be sent. Values must be String, any other type will
     *                   be ignored.
     * @throws IllegalArgumentException
     * @throws IOException
     */
    public void send(String to, String msgId, long timeToLive, Bundle data) throws IOException {
        if (TextUtils.isEmpty(to)) throw new IllegalArgumentException("Invalid 'to'");
        Intent intent = new Intent(ACTION_GCM_SEND);
        intent.setPackage(GMS_PACKAGE_NAME);
        if (data != null) intent.putExtras(data);
        intent.putExtra(EXTRA_APP, getSelfAuthIntent());
        intent.putExtra(EXTRA_SEND_TO, to);
        intent.putExtra(EXTRA_MESSAGE_ID, msgId);
        intent.putExtra(EXTRA_TTL, timeToLive);
        intent.putExtra(EXTRA_DELAY, -1);
        //intent.putExtra(EXTRA_SEND_FROM, TODO)
        context.sendOrderedBroadcast(intent, PERMISSION_GTALK);
    }

    /**
     * Send an upstream ("device to cloud") message. You can only use the upstream feature
     * if your GCM implementation uses the XMPP-based
     * <a href="http://developer.android.com/google/gcm/ccs.html">Cloud Connection Server</a>.
     * <p/>
     * When there is an active connection the message will be sent immediately, otherwise the
     * message will be queued for the maximum interval.
     *
     * @param to    string identifying the receiver of the message in the format of
     *              <code>SENDER_ID@gcm.googleapis.com</code>. The <code>SENDER_ID</code> should be one of the sender
     *              IDs used when calling  {@link com.google.android.gms.iid.InstanceID#getToken(java.lang.String, java.lang.String)}
     * @param msgId ID of the message. This is generated by the application. It must be
     *              unique for each message. This allows error callbacks and debugging.
     * @param data  key/value pairs to be sent. Values must be String—any other type will
     *              be ignored.
     * @throws IllegalArgumentException
     * @throws IOException
     */
    public void send(String to, String msgId, Bundle data) throws IOException {
        send(to, msgId, -1, data);
    }

    /**
     * Unregister the application. Calling <code>unregister()</code> stops any
     * messages from the server. This is a blocking call—you shouldn't call
     * it from the UI thread.
     * <p/>
     * You should rarely (if ever) need to call this method. Not only is it
     * expensive in terms of resources, but it invalidates all your registration IDs
     * returned from register() or subscribe(). This should not be done
     * unnecessarily. A better approach is to simply have your server stop
     * sending messages.
     *
     * @throws IOException if we can't connect to server to unregister.
     * @deprecated Instead use
     * {@link com.google.android.gms.iid.InstanceID#deleteToken(java.lang.String, java.lang.String)} or
     * {@link com.google.android.gms.iid.InstanceID#deleteInstanceID()}.
     */
    @Deprecated
    public void unregister() throws IOException {
        if (Looper.getMainLooper() == Looper.myLooper()) throw new IOException(ERROR_MAIN_THREAD);
        InstanceID.getInstance(context).deleteInstanceID();
    }

}