package com.jonlenes.appemprestimo;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.provider.CalendarContract;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.jonlenes.appemprestimo.Geral.DateUtil;
import com.jonlenes.appemprestimo.Modelo.Emprestimo;
import com.jonlenes.appemprestimo.Modelo.EmprestimoBo;
import com.jonlenes.appemprestimo.Modelo.EmprestimoDao;
import com.jonlenes.appemprestimo.Modelo.Parcela;
import com.jonlenes.appemprestimo.Modelo.StatusParcela;

import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ParcelasPagarActivity extends AppCompatActivity {

    private ListView lvParcelasPagar;

    private Date dateInicial;
    private Date dateFinal;
    private List<Emprestimo> emprestimos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parcelas_pagar);

        lvParcelasPagar = (ListView) findViewById(R.id.lvParcelasPagar);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
        dateInicial = calendar.getTime();
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        dateFinal = calendar.getTime();

        Toast.makeText(this, dateInicial.toString(), Toast.LENGTH_LONG).show();
        Toast.makeText(this, dateFinal.toString(), Toast.LENGTH_LONG).show();

        new BuscaParcelasAsyncTask().execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_parcelas_pagar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_pagar_todas) {

            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private class BuscaParcelasAsyncTask extends AsyncTask<Void, Void, List<Emprestimo> > {
        private final ProgressDialog progressDialog;
        private Exception exception;

        public BuscaParcelasAsyncTask() {
            progressDialog = new ProgressDialog(ParcelasPagarActivity.this);
            progressDialog.setMessage("Buscando parcelas a pagar...");
            progressDialog.setCancelable(false);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            TextView tvEmpty = (TextView) ParcelasPagarActivity.this.findViewById(R.id.tvEmpty);
            if (tvEmpty != null) tvEmpty.setVisibility(View.INVISIBLE);

            progressDialog.show();
        }


        @Override
        protected List<Emprestimo> doInBackground(Void... params) {
            try {

                return new EmprestimoBo().getEmprestimoComParcela(dateInicial, dateFinal, StatusParcela.pagar.ordinal());

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

                emprestimos = list;

                if (list.isEmpty()) {

                    TextView tvEmpty = (TextView) ParcelasPagarActivity.this.findViewById(R.id.tvEmpty);
                    if (tvEmpty != null) tvEmpty.setVisibility(View.VISIBLE);

                } else {

                    lvParcelasPagar.setAdapter(new AdapterListParcela(list));
                }

            } else
                TreatException.treat(ParcelasPagarActivity.this, exception);
        }
    }


    class AdapterListParcela extends BaseAdapter {
        private final List<Emprestimo> list;

        public AdapterListParcela(List<Emprestimo> list) {
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
                convertView = ParcelasPagarActivity.this.getLayoutInflater().inflate(R.layout.lv_parcelas_pagar_row, null);
                viewHolder = new ViewHolder();

                viewHolder.tvNumeroParcela = (TextView) convertView.findViewById(R.id.tvNumeroParcela);
                viewHolder.tvValorParcela = (TextView) convertView.findViewById(R.id.tvValorParcela);
                viewHolder.tvDescricaoEmprestimo = (TextView) convertView.findViewById(R.id.tvDescricaoEmprestimo);
                viewHolder.tvDataParcela = (TextView) convertView.findViewById(R.id.tvDataParcela);

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            Emprestimo emprestimo = list.get(position);
            Parcela parcela = emprestimo.getParcelas().get(0);

            Double valorPagar = parcela.getValorPrincipal() + parcela.getValorJuros() + parcela.getValorMultaAtraso();

            viewHolder.tvNumeroParcela.setText(parcela.getNumero() + "/" + emprestimo.getQtdeParcelas());
            viewHolder.tvValorParcela.setText(NumberFormat.getCurrencyInstance().format(valorPagar));
            viewHolder.tvDescricaoEmprestimo.setText(emprestimo.getDescricao());
            viewHolder.tvDataParcela.setText(DateUtil.formatDate(parcela.getDataVencimento()));

            return convertView;
        }

        private class ViewHolder {
            TextView tvNumeroParcela;
            TextView tvValorParcela;
            TextView tvDescricaoEmprestimo;
            TextView tvDataParcela;
        }

    }
}
