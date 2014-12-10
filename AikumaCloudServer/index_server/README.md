# Index Server

A Java-based application providing a RESTful API for accessing the Aikuma metadata index over the web.


## Configuring the index server

### Google Credentials

There are two separate Google credentials that need to be configured for the application to function properly, one for
service account that accesses the Fusion Tables API and one that allows the Android application to access the Index
Server API.

Both of these credentials are created using the [Google Developers Console](https://console.developers.google.com/), and
need to be created in the same project as the Aikuma client application. Once you've selected the correct project,
select the Credentials item on the left side of the screen, under the under the **APIs & auth** heading.

#### Configuring the Service Account

The service account is used to query the Fusion Tables API from the index server.

##### Creating the Client ID

* Click **Create new Client ID**
* Select **Service account** for Application Type
* Click **Create Client ID**

The console will create the service account. You'll be prompted to download a file called [Project Name]-[hex string].p12.
This is a PKCS12 format private key file. The console will also display a password for the private key file, which is
(apparently always) "notasecret". If you scroll to the Service Account entry you've just created, you'll find an email
address associated with the account, as well as an option to generate a new PKCS12 key.

##### Adding the relevant entries to index_server.properties

The following properties relate to the credentials you just created:

* **service_email** - This is the email address associated with the service account created above. Make sure to use the
email address rather than the client ID.
* **private_key_path** - The path to the P12 private key file you were prompted to download above.
* **private_key_password** - The password for the P12 file that was displayed after creating the service account/private
key. Should probably be "notasecret".

Note: You'll also need to add a set of OAUTH2 scopes for the relevant Google APIs. I use this:

> scopes=https://www.googleapis.com/auth/fusiontables https://www.googleapis.com/auth/drive.metadata.readonly

#### Configuring Authentication

This is to configure the JWT-based authentication of Android clients accessing the index server HTTP interface.
To set up the authentication, you'll need to create a client ID for the web application:

##### Generating the Client ID

* Click **Create new Client ID**
* Select **Web application** for Application Type
* Click **Create Client ID**

The Authorized JavaScript Origins and Authorized Redirect URIs boxes can be left as is; they're irrelevant for our use.

#### Adding the relevant entries to index_server.properties

The following properties relate to the credentials you jsut created:

* **audience** - this is the Client ID value for the web application credentials you created above; make sure it's the
client ID and not the email address.

Note: you'll need to add the client ID of the Android application, as follows;

>valid_app_client_ids=763016806096-465lg8g0lo10olnh8kttpd27c18ttcvu.apps.googleusercontent.com

finally, to make the auth happen, add:

>require_auth=yes

### Other setttings

The remaining settings cover configuring the table_id to use for querying and serving the REST API over SSL:

* **table_id** this is the globally unique table_id for the Fusion Index metadata table.

* **use_ssl** - if *yes*, use SSL to serve the REST API
* **keystore_file** - a file URL pointing to a Java keystore file containing the certificate ot use for SSL
* **keystore_password** - the password for the certificate keystore above




## Notes

If you've enabled authentication, you should also encrypt server traffic with SSL, either directly in the index server
config (as above) or through a front end proxy. The authentication tokens are cryptographically signed by Google, but
are otherwise unencrypted and can easily be reused by another client if sniffed over the network. Requiring auth without
SSL is essentially useless.
