package sinha.deepankar.nix.thebuzzer;

import android.os.AsyncTask;

import java.net.Socket;

/**
 * Created by User on 02-03-2016.
 */
public class Server extends AsyncTask {
    Socket socket;
    Server(Socket socket)
    {
        this.socket=socket;
    }

    @Override
    protected Object doInBackground(Object[] params) {

        return null;
    }

}
