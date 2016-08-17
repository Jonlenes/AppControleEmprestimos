package com.jonlenes.appemprestimo;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.jonlenes.appemprestimo.Modelo.Emprestimo;
import com.jonlenes.appemprestimo.Modelo.EmprestimoBo;
import com.jonlenes.appemprestimo.Modelo.EmprestimoDao;

import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle("Empréstimos");

        try {

            FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
            if(fab != null) fab.setOnClickListener(clickListenerNovoEmprestimo);

            ListView lvEmprestimos = (ListView) findViewById(R.id.lvEmprestimos);
            if (lvEmprestimos != null) lvEmprestimos.setOnItemClickListener(itemClickEmprestimo);

        } catch (Exception e) {
            TreatException.treat(MainActivity.this, e);
        }

        //Inicialização da base de dados
        new InitBdAsyncTask().execute();

    }

    @Override
    protected void onResume() {
        super.onResume();

        new BuscaEmprestimosAsyncTask().execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    AdapterView.OnItemClickListener itemClickEmprestimo =  new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent intentParcelas = new Intent(MainActivity.this, ParcelasActivity.class);
            intentParcelas.putExtra("idEmprestimo", ((Emprestimo) parent.getItemAtPosition(position)).getId());
            startActivity(intentParcelas);
        }
    };


    private View.OnClickListener clickListenerNovoEmprestimo = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            new DialogFragment() {

                private AlertDialog dialog;
                private View view;

                private EditText edtDescricaoEmp;
                private EditText edtValorEmp;
                private EditText edtDataEmp;
                private EditText edtQtdeParcelas;
                private EditText edtDatePrimeiraParcela;

                private Long idEmprestimo;


                @NonNull
                @Override
                public Dialog onCreateDialog(Bundle savedInstanceState) {
                    super.onCreate(savedInstanceState);

                    try {

                        LayoutInflater inflater = getActivity().getLayoutInflater();
                        view = inflater.inflate(R.layout.dialog_new_emprestimo, null);

                        //Referêncio os componentes visuais
                        edtDescricaoEmp = (EditText) view.findViewById(R.id.edtDescricaoEmp);
                        edtValorEmp = (EditText) view.findViewById(R.id.edtValorEmp);
                        edtDataEmp = (EditText) view.findViewById(R.id.edtDataEmp);
                        edtQtdeParcelas = (EditText) view.findViewById(R.id.edtQtdeParcelas);
                        edtDatePrimeiraParcela = (EditText) view.findViewById(R.id.edtDatePrimeiraParcela);

                        //Valores padrão
                        edtDataEmp.setText(Util.formatDate(new Date()));
                        edtDatePrimeiraParcela.setText(Util.formatDate(new Date()));

                        //Click
                        edtDataEmp.setOnClickListener(new ClickDate(edtDataEmp));
                        edtDatePrimeiraParcela.setOnClickListener(new ClickDate(edtDatePrimeiraParcela));

                        //Criando o dialog
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setView(view)
                                .setPositiveButton("OK", null)
                                .setNegativeButton("Cancelar", null);

                        //Setando o titulo
                        Bundle bundle = getArguments();
                        idEmprestimo = (bundle != null ? bundle.getLong("idEmprestimo", -1) : -1L);
                        if (idEmprestimo != -1) builder.setTitle("Atualização de empréstimo");
                        else builder.setTitle("Novo empréstimo");

                        dialog = builder.create();
                        dialog.show();

                        //Butão de inserção
                        dialog.getButton(Dialog.BUTTON_POSITIVE).setOnClickListener(clickListenerInserir);

                        //if (idReserve != -1)
                        //    new SearchReserveAsyncTask().execute();

                    } catch (Exception e) {
                        TreatException.treat(MainActivity.this, e);
                    }

                    return dialog;
                }

                @Override
                public void onDismiss(DialogInterface dialog) {
                    super.onDismiss(dialog);
                    new BuscaEmprestimosAsyncTask().execute();
                }

                class ClickDate implements View.OnClickListener {

                    private EditText edtDate;

                    ClickDate(EditText edtDate) {
                        this.edtDate = edtDate;
                    }

                    @Override
                    public void onClick(View v) {
                        class DatePickerFragment extends DialogFragment
                                implements DatePickerDialog.OnDateSetListener {

                            @Override
                            public Dialog onCreateDialog(Bundle savedInstanceState) {
                                Calendar c = Calendar.getInstance();
                                if (edtDate.getText().length() > 0)
                                    c.setTime(Util.parseDate(edtDate.getText().toString()));
                                return new DatePickerDialog(getActivity(), this, c.get(Calendar.YEAR),
                                        c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
                            }

                            public void onDateSet(DatePicker view, int year, int month, int day) {
                                Calendar calendar = Calendar.getInstance();
                                calendar.set(year, month, day);

                                edtDate.setText(Util.formatDate(calendar.getTime()));
                            }
                        }

                        new DatePickerFragment().show(MainActivity.this.getSupportFragmentManager(), "dialog");
                    }
                };

                View.OnClickListener clickListenerInserir = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {

                            if (validFields()) {

                                new InsertEmprestimoAsyncTask().execute(new Emprestimo(idEmprestimo,
                                        edtDescricaoEmp.getText().toString(),
                                        Double.parseDouble(edtValorEmp.getText().toString()),
                                        Util.parseDate(edtDataEmp.getText().toString()),
                                        Long.parseLong(edtQtdeParcelas.getText().toString()),
                                        Util.parseDate(edtDatePrimeiraParcela.getText().toString())));

                                dialog.dismiss();
                            }


                        } catch (Exception e) {
                            TreatException.treat(MainActivity.this, e);
                        }
                    }
                };


                private boolean validFields() {
                    if (edtDescricaoEmp.getText().toString().isEmpty()) {
                        edtDescricaoEmp.setError("A descrisão deve ser preenchida.");
                        return false;
                    }

                    if (edtValorEmp.getText().toString().isEmpty() || Double.parseDouble(
                            edtValorEmp.getText().toString()) <= 0) {
                        edtValorEmp.setError("O valor deve ser maior do que zero.");
                        return false;
                    }

                    if (edtQtdeParcelas.getText().toString().isEmpty() || Integer.parseInt(
                            edtQtdeParcelas.getText().toString()) <= 0) {
                        edtQtdeParcelas.setError("A quantidade de parcelas deve ser maior do que zero.");
                        return false;
                    }

                    return true;
                }

                class InsertEmprestimoAsyncTask extends AsyncTask<Emprestimo, Void, Void> {
                    private ProgressDialog progressDialog;
                    private Exception exception;

                    public InsertEmprestimoAsyncTask() {
                        progressDialog = new ProgressDialog(MainActivity.this);
                        if (idEmprestimo != -1)
                            progressDialog.setMessage("Atualizando...");
                        else
                            progressDialog.setMessage("Inserindo...");
                        progressDialog.setCancelable(false);
                    }

                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                        progressDialog.show();
                    }

                    @Override
                    protected Void doInBackground(Emprestimo... params) {
                        try {

                            new EmprestimoBo().insert(params[0]);

                        } catch (Exception e) {
                            exception = e;
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        super.onPostExecute(aVoid);

                        progressDialog.dismiss();

                        if (exception != null)
                            TreatException.treat(MainActivity.this, exception);
                        else
                            dialog.dismiss();
                    }
                }


                /*private class SearchClientsAsyncTask extends AsyncTask<Void, Void, List<Client>> {
                    private ProgressDialog progressDialog;
                    private Exception exception;

                    public SearchClientsAsyncTask() {
                        progressDialog = new ProgressDialog(activity);
                        progressDialog.setMessage("Buscando clientes...");
                        progressDialog.setCancelable(false);
                    }

                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                        progressDialog.show();
                    }

                    @Override
                    protected List<Client> doInBackground(Void... params) {
                        try {

                            return new ClientDao().getAllWithoutImage();

                        } catch (Exception e) {
                            exception = e;
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(List<Client> list) {
                        super.onPostExecute(list);

                        progressDialog.dismiss();

                        if (exception == null) {
                            ArrayAdapter<Client> adapterClients = new ArrayAdapter<>(activity,
                                    android.R.layout.simple_spinner_item, list);
                            adapterClients.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spnClientReserve.setAdapter(adapterClients);
                        } else {
                            TreatException.treat(activity, exception);
                        }

                    }
                }

                private class SearchLocalsAsyncTask extends AsyncTask<Void, Void, List<Local>> {
                    private ProgressDialog progressDialog;
                    private Exception exception;

                    public SearchLocalsAsyncTask() {
                        progressDialog = new ProgressDialog(activity);
                        progressDialog.setMessage("Buscando locais...");
                        progressDialog.setCancelable(false);
                    }

                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                        progressDialog.show();
                    }

                    @Override
                    protected List<Local> doInBackground(Void... params) {
                        try {

                            return new LocalDao().getAll();

                        } catch (Exception e) {
                            exception = e;
                        }

                        return null;
                    }

                    @Override
                    protected void onPostExecute(List<Local> locals) {
                        super.onPostExecute(locals);

                        progressDialog.dismiss();

                        if (exception == null) {

                            ArrayAdapter<Local> adapterLocal = new ArrayAdapter<>(activity,
                                    android.R.layout.simple_spinner_item, locals);
                            adapterLocal.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spnLocalReserve.setAdapter(adapterLocal);

                        } else {
                            TreatException.treat(activity, exception);
                        }
                    }
                }

                private class SearchReserveAsyncTask extends AsyncTask<Void, Void, Reserve> {
                    private final ProgressDialog progressDialog;
                    private Exception exception;

                    public SearchReserveAsyncTask() {
                        progressDialog = new ProgressDialog(activity);
                        progressDialog.setMessage("Buscando reserva...");
                        progressDialog.setCancelable(false);
                    }

                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                        progressDialog.show();
                    }

                    @Override
                    protected Reserve doInBackground(Void... params) {
                        try {

                            return new ReserveDao().getReserve(idReserve);

                        } catch (Exception e) {
                            exception = e;
                        }

                        return null;
                    }

                    @Override
                    protected void onPostExecute(Reserve Reserve) {
                        super.onPostExecute(Reserve);

                        if (exception == null) {

                            int i = 0;
                            while (i < 5 && (spnClientReserve.getAdapter().isEmpty() || spnLocalReserve.getAdapter().isEmpty())) {
                                try {
                                    wait(1000);
                                    ++i;
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }

                            if (i == 5) {
                                Toast.makeText(activity, "Não foi possível buscar a reserva.", Toast.LENGTH_LONG).show();
                                dismiss();
                            } else {

                                edtDateReserve.setText(Util.formatDate(Reserve.getDateDay()));
                                edtHoursReserva.setText(Util.formatTime(Reserve.getStartTime()));
                                edtDurationReserve.setText(String.valueOf(Reserve.getDuration()));
                                spnClientReserve.setSelection(getIndexItemSpinner(spnClientReserve, Reserve.getClient().getId()));
                                spnLocalReserve.setSelection(getIndexItemSpinner(spnLocalReserve, Reserve.getLocal().getId()));

                            }


                        } else {
                            TreatException.treat(activity, exception);
                        }

                        progressDialog.dismiss();

                    }
                }*/
            }.show(MainActivity.this.getSupportFragmentManager(), "dialog");
        }
    };


    private class InitBdAsyncTask extends AsyncTask<Void, Void, Void> {

        private ProgressDialog progressDialog;
        private Exception exception;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setMessage("Inicializando...");
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                DbHelper.newInstance(MainActivity.this);
            } catch (Exception e) {
                exception = e;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            progressDialog.dismiss();
            if (exception != null)
                TreatException.treat(MainActivity.this, exception);
        }
    }

    private class BuscaEmprestimosAsyncTask extends AsyncTask<Void, Void, List<Emprestimo> > {
        private final ProgressDialog progressDialog;
        private Exception exception;

        public BuscaEmprestimosAsyncTask() {
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setMessage("Buscando empréstimos...");
            progressDialog.setCancelable(false);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            TextView tvEmpty = (TextView) MainActivity.this.findViewById(R.id.tvEmpty);
            if (tvEmpty != null) tvEmpty.setVisibility(View.INVISIBLE);
            progressDialog.show();
        }


        @Override
        protected List<Emprestimo> doInBackground(Void... params) {
            try {

                return new EmprestimoDao().getAll();

            } catch (Exception e) {
                exception = e;
            }

            return null;
        }

        @Override
        protected void onPostExecute(List<Emprestimo> list) {
            super.onPostExecute(list);

            progressDialog.dismiss();

            if (exception == null) {

                if (list.isEmpty()) {

                    TextView tvEmpty = (TextView) MainActivity.this.findViewById(R.id.tvEmpty);
                    if (tvEmpty != null) tvEmpty.setVisibility(View.VISIBLE);

                } else {

                    ListView lvEmprestimos = (ListView) MainActivity.this.findViewById(R.id.lvEmprestimos);
                    if (lvEmprestimos != null) lvEmprestimos.setAdapter(new AdapterListEmprestimo(list));
                }

            } else
                TreatException.treat(MainActivity.this, exception);
        }
    }

    class AdapterListEmprestimo extends BaseAdapter {
        private final List<Emprestimo> list;

        public AdapterListEmprestimo(List<Emprestimo> list) {
            super();
            this.list = list;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;

            if (convertView == null) {
                convertView = MainActivity.this.getLayoutInflater().inflate(R.layout.lv_emprestimos_row, null);
                viewHolder = new ViewHolder();

                viewHolder.tvDescricaoEmp = (TextView) convertView.findViewById(R.id.tvDataParcela);
                viewHolder.tvValorEmp = (TextView) convertView.findViewById(R.id.tvValorParcela);
                viewHolder.tvDateEmp = (TextView) convertView.findViewById(R.id.tvDateEmp);
                viewHolder.tvStatusEmp = (TextView) convertView.findViewById(R.id.tvStatusEmp);

                convertView.setTag(viewHolder);
            }
            else
            {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            Emprestimo emprestimo = list.get(position);

            viewHolder.tvDescricaoEmp.setText(emprestimo.getDescricao());
            viewHolder.tvValorEmp.setText(NumberFormat.getCurrencyInstance().format(emprestimo.getValor()));
            viewHolder.tvDateEmp.setText(Util.formatDate(emprestimo.getData()));
            viewHolder.tvStatusEmp.setText("A pagar");

            return convertView;
        }

        private class ViewHolder {
            TextView tvDescricaoEmp;
            TextView tvValorEmp;
            TextView tvDateEmp;
            TextView tvStatusEmp;
        }
    }
}
