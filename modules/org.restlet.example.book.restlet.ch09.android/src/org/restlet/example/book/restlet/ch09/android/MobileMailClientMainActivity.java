package org.restlet.example.book.restlet.ch09.android;

import org.restlet.Client;
import org.restlet.data.Protocol;
import org.restlet.engine.Engine;
import org.restlet.example.book.restlet.ch09.common.MailsRepresentation;
import org.restlet.example.book.restlet.ch09.common.MailsResource;
import org.restlet.ext.net.HttpClientHelper;
import org.restlet.ext.jackson.JacksonConverter;
import org.restlet.resource.ClientResource;

import android.app.ListActivity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * Android activity that retrieves the list of email from the GAE backend.
 * 
 * @author Jerome Louvel
 */
public class MobileMailClientMainActivity extends ListActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mobile_mail_client_main);

        ListView emailList = getListView();
        emailList.setTextFilterEnabled(true);

        // Set-up the Restlet Engine
        Engine.getInstance().getRegisteredClients().clear();
        Engine.getInstance().getRegisteredClients()
                .add(new HttpClientHelper(new Client(Protocol.HTTP)));
        Engine.getInstance().getRegisteredConverters()
                .add(new JacksonConverter());

        // Retrieve the list of mails from the remote web API
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                ClientResource clientResource = new ClientResource(
                        "http://reia-ch09.appspot.com/accounts/chunkylover53/mails/");
                MailsResource mailsResource = clientResource
                        .wrap(MailsResource.class);
                MailsRepresentation emails = mailsResource.retrieve();
                final String[] subjects = new String[emails.getEmails().size()];

                for (int i = 0; i < emails.getEmails().size(); i++) {
                    System.out.println(emails.getEmails().get(i));
                    subjects[i] = emails.getEmails().get(i).getSubject();
                }

                runOnUiThread(new Runnable() {
                    public void run() {
                        setListAdapter(new ArrayAdapter<String>(
                                MobileMailClientMainActivity.this,
                                R.layout.activity_mobile_mail_client_main,
                                R.id.list_item, subjects));
                    }
                });

                return null;
            }
        };

        task.execute(null, null, null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater()
                .inflate(R.menu.activity_mobile_mail_client_main, menu);
        return true;
    }

}
