package com.jonlenes.appemprestimo;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.jonlenes.appemprestimo.Geral.DateUtil;
import com.jonlenes.appemprestimo.Modelo.Emprestimo;
import com.jonlenes.appemprestimo.Modelo.EmprestimoBo;
import com.jonlenes.appemprestimo.Modelo.EmprestimoDao;

import java.text.NumberFormat;
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
            if (lvEmprestimos != null) {
                lvEmprestimos.setOnItemClickListener(itemClickEmprestimo);
                lvEmprestimos.setOnItemLongClickListener(itemLongClickListenerEmprestimo);
            }

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

        if (id == R.id.action_pagar_todas) {
            Intent intent = new Intent(MainActivity.this, ParcelasPagarActivity.class);
            startActivity(intent);
            return false;
        }

        return super.onOptionsItemSelected(item);
    }

    public void atualizaEmprestimos() {
        new BuscaEmprestimosAsyncTask().execute();
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

            new DialogNewEmprestimo().show(MainActivity.this.getSupportFragmentManager(), "dialog");
        }
    };

    AdapterView.OnItemLongClickListener itemLongClickListenerEmprestimo = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(final AdapterView<?> parent, View view, int position, long id) {
            try {
                final Emprestimo emprestimo = (Emprestimo) parent.getItemAtPosition(position);

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder
                        .setTitle("Opções")
                        .setItems(R.array.dialog_options_emprestimo, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0: //Editar
                                        showDialogAtualizaEmprestimo(emprestimo);
                                        break;

                                    case 1: //Deletar
                                        showDeleteEmprestimo(emprestimo);
                                        break;

                                }
                            }
                        }).create().show();
            } catch (Exception e) {
                TreatException.treat(MainActivity.this, e);
            }

            return true;
        }
    };

    private void showDialogAtualizaEmprestimo(Emprestimo emprestimo) {
        DialogNewEmprestimo dialogNewEmprestimo = new DialogNewEmprestimo();
        Bundle bundle = new Bundle();

        bundle.putLong("idEmprestimo", emprestimo.getId());
        dialogNewEmprestimo.setArguments(bundle);

        dialogNewEmprestimo.show(MainActivity.this.getSupportFragmentManager(), "dialog");
    }

    private void showDeleteEmprestimo(final Emprestimo emprestimo) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Confirmação");
        builder.setMessage("Deseja realmente excluir?");
        builder.setNegativeButton("Não", null);
        builder.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                new DeleteEmprestimoAsyncTask().execute(emprestimo.getId());
            }
        });
        builder.create().show();
    }



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
                }

                ListView lvEmprestimos = (ListView) MainActivity.this.findViewById(R.id.lvEmprestimos);
                if (lvEmprestimos != null) lvEmprestimos.setAdapter(new AdapterListEmprestimo(list));

            } else
                TreatException.treat(MainActivity.this, exception);
        }
    }

    private class DeleteEmprestimoAsyncTask extends AsyncTask<Long, Void, Void> {
        private ProgressDialog progressDialog;
        private Exception exception;

        public DeleteEmprestimoAsyncTask() {
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setMessage("Deletando...");
            progressDialog.setCancelable(false);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
        }


        @Override
        protected Void doInBackground(Long... params) {
            try {

                new EmprestimoBo().delete(params[0]);

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

            new BuscaEmprestimosAsyncTask().execute();
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

                viewHolder.tvDescricaoEmp = (TextView) convertView.findViewById(R.id.tvNumeroParcela);
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
            viewHolder.tvDateEmp.setText(DateUtil.formatDate(emprestimo.getData()));
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
