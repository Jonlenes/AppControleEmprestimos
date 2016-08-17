package com.jonlenes.appemprestimo;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.jonlenes.appemprestimo.Modelo.Emprestimo;
import com.jonlenes.appemprestimo.Modelo.EmprestimoDao;
import com.jonlenes.appemprestimo.Modelo.Parcela;
import com.jonlenes.appemprestimo.Modelo.ParcelaDao;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ParcelasActivity extends AppCompatActivity {

    private Long idEmprestimo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parcelas);

        idEmprestimo = getIntent().getLongExtra("idEmprestimo", -1);
        if (idEmprestimo == -1)
            finish();
        else
            new BuscaParcelasAsyncTask().execute();
    }


    private class BuscaParcelasAsyncTask extends AsyncTask<Void, Void, List<Parcela> > {
        private final ProgressDialog progressDialog;
        private Exception exception;

        public BuscaParcelasAsyncTask() {
            progressDialog = new ProgressDialog(ParcelasActivity.this);
            progressDialog.setMessage("Buscando parcelas...");
            progressDialog.setCancelable(false);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            TextView tvEmpty = (TextView) ParcelasActivity.this.findViewById(R.id.tvEmpty);
            if (tvEmpty != null) tvEmpty.setVisibility(View.INVISIBLE);
            progressDialog.show();
        }


        @Override
        protected List<Parcela> doInBackground(Void... params) {
            try {

                return new ParcelaDao().getAllByEmprestimo(idEmprestimo);

            } catch (Exception e) {
                exception = e;
            }

            return null;
        }

        @Override
        protected void onPostExecute(List<Parcela> list) {
            super.onPostExecute(list);

            progressDialog.dismiss();

            if (exception == null) {

                if (list.isEmpty()) {

                    TextView tvEmpty = (TextView) ParcelasActivity.this.findViewById(R.id.tvEmpty);
                    if (tvEmpty != null) tvEmpty.setVisibility(View.VISIBLE);

                } else {

                    ListView lvParcelas = (ListView) ParcelasActivity.this.findViewById(R.id.lvParcelas);
                    if (lvParcelas != null) lvParcelas.setAdapter(new AdapterListParcela(list));
                }

            } else
                TreatException.treat(ParcelasActivity.this, exception);
        }
    }

    class AdapterListParcela extends BaseAdapter
    {
        private final List<Parcela> list;

        public AdapterListParcela(List<Parcela> list) {
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
                convertView = ParcelasActivity.this.getLayoutInflater().inflate(R.layout.lv_parcelas_row, null);
                viewHolder = new ViewHolder();

                viewHolder.tvDataParcela = (TextView) convertView.findViewById(R.id.tvDataParcela);
                viewHolder.tvValorParcela = (TextView) convertView.findViewById(R.id.tvValorParcela);
                viewHolder.tvStatusParcela = (TextView) convertView.findViewById(R.id.tvStatusParcela);

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            Parcela parcela = list.get(position);
            Double valorPagar = parcela.getValorPrincipal() + parcela.getValorJuros() + parcela.getValorMultaAtraso();

            viewHolder.tvDataParcela.setText(Util.formatDate(parcela.getDataVencimento()));
            viewHolder.tvValorParcela.setText(NumberFormat.getCurrencyInstance().format(valorPagar));
            viewHolder.tvStatusParcela.setText("A pagar");

            return convertView;
        }

        private class ViewHolder {
            TextView tvDataParcela;
            TextView tvValorParcela;
            TextView tvStatusParcela;
        }
    }
}