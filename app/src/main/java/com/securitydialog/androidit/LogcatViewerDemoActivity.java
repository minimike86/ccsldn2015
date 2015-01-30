package com.securitydialog.androidit;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class LogcatViewerDemoActivity extends ListActivity {
    private LogStringAdaptor adaptor = null;
    private ArrayList<String> logarray = null;
    private LogReaderTask logReaderTask = null;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        logarray = new ArrayList<String>();
        adaptor = new LogStringAdaptor(this, R.id.txtLogString, logarray);

        setListAdapter(adaptor);

        logReaderTask = new LogReaderTask();
        logReaderTask.execute();
    }

    @Override
    protected void onDestroy() {
        logReaderTask.stopTask();

        super.onDestroy();
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        final AlertDialog.Builder builder = new AlertDialog.Builder(LogcatViewerDemoActivity.this);
        String text = ((String) ((TextView) v).getText());

        builder.setMessage(text);

        builder.show();
    }

    private int getLogColor(String type) {
        int color = Color.BLUE;

        if (type.equals("D")) {
            color = Color.rgb(0, 0, 200);
        } else if (type.equals("W")) {
            color = Color.rgb(128, 0, 0);
        } else if (type.equals("E")) {
            color = Color.rgb(255, 0, 0);
            ;
        } else if (type.equals("I")) {
            color = Color.rgb(0, 128, 0);
            ;
        }

        return color;
    }

    private class LogStringAdaptor extends ArrayAdapter<String> {
        private List<String> objects = null;

        public LogStringAdaptor(Context context, int textviewid, List<String> objects) {
            super(context, textviewid, objects);

            this.objects = objects;
        }

        @Override
        public int getCount() {
            return ((null != objects) ? objects.size() : 0);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public String getItem(int position) {
            return ((null != objects) ? objects.get(position) : null);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;

            if (null == view) {
                LayoutInflater vi = (LayoutInflater) LogcatViewerDemoActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = vi.inflate(R.layout.layout, null);
            }

            String data = objects.get(position);

            if (null != data) {
                TextView textview = (TextView) view.findViewById(R.id.txtLogString);
                String type = data.substring(0, 1);
                String line = data.substring(2);

                textview.setText(line);
                textview.setTextColor(getLogColor(type));
            }

            return view;
        }
    }

    private class LogReaderTask extends AsyncTask<Void, String, Void> {

        private final int BUFFER_SIZE = 4096;

        private boolean isRunning = true;
        private Process logProcess = null;
        private BufferedReader reader = null;
        private String[] line = null;

        @Override
        protected Void doInBackground(Void... params) {
            try {
                logProcess = Runtime.getRuntime().exec("logcat");
            } catch (IOException e) {
                e.printStackTrace();

                isRunning = false;
            }

            try {
                reader = new BufferedReader(new InputStreamReader(
                        logProcess.getInputStream()), BUFFER_SIZE);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();

                isRunning = false;
            }

            String line;

            try {
                while (isRunning) {
                    // Read logcat line
                    line = reader.readLine();
                    // Build syslog from logcat line
                    Random r = new Random();
                    Syslog syslog = new Syslog(r.nextInt(191), line);
                    // Update ListView
                    publishProgress(line + "\n" + "Syslog: " + syslog.toString());
                    // Send syslog message
                    Socket socket = null;
                    try {
                        socket = new Socket("10.100.94.71", 1468); //514UDP or 1468TCP
                        PrintWriter out = new PrintWriter(socket.getOutputStream());
                        out.print(syslog.toString()+"\r\n");
                        out.flush(); // Send it now.
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (socket != null) {
                            try {
                                socket.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                isRunning = false;
            } catch (NullPointerException e) {
                e.printStackTrace();
                isRunning = false;
            }

            return null;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);

            adaptor.add(values[0]);
        }

        public void stopTask() {
            isRunning = false;
            logProcess.destroy();
        }
    }

}