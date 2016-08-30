package com.jonlenes.appemprestimo;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.jonlenes.appemprestimo.Geral.ClickDate;
import com.jonlenes.appemprestimo.Geral.DateUtil;
import com.jonlenes.appemprestimo.Modelo.Parcela;
import com.jonlenes.appemprestimo.Modelo.ParcelaBo;
import com.jonlenes.appemprestimo.Modelo.StatusParcela;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ParcelasActivity extends AppCompatActivity {

    private Long idEmprestimo;
    private List<Parcela> parcelas;
    private Double valorTotalParcelas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parcelas);

        ListView lvParcelas = (ListView) findViewById(R.id.lvParcelas);
        if (lvParcelas != null) {
            lvParcelas.setOnItemClickListener(itemClickParcela);
            lvParcelas.setOnItemLongClickListener(itemLongClickListenerParcela);
        }
        RadioGroup radioGroupFiltro = (RadioGroup) findViewById(R.id.rgFiltroParcelas) ;
        if (radioGroupFiltro != null) radioGroupFiltro.setOnCheckedChangeListener(checkedRadioGroupFiltro);
        valorTotalParcelas = 0.0;

        idEmprestimo = getIntent().getLongExtra("idEmprestimo", -1);
        if (idEmprestimo == -1)
            finish();
        else
            new BuscaParcelasAsyncTask().execute();

        atualizaViewTotalizadora();
    }

    private void atualizaViewTotalizadora() {
        TextView tvValorTotal = (TextView) findViewById(R.id.tvValorTotal);
        if (tvValorTotal != null)
            tvValorTotal.setText(NumberFormat.getCurrencyInstance().format(valorTotalParcelas));
    }

    AdapterView.OnItemClickListener itemClickParcela =  new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Parcela parcela = (Parcela) parent.getItemAtPosition(position);
            String messege = "Valor principal: " + NumberFormat.getCurrencyInstance().format(
                    parcela.getValorPrincipal()) + "\n" +
                    "Valor juros: " + NumberFormat.getCurrencyInstance().format(
                    parcela.getValorJuros()) + "\n" +
                    "Valor multa atraso: " + NumberFormat.getCurrencyInstance().format(
                    parcela.getValorMultaAtraso());

            AlertDialog.Builder builder = new AlertDialog.Builder(ParcelasActivity.this);
            builder.setTitle("Valores");
            builder.setMessage(messege);
            builder.setPositiveButton("Ok", null);
            builder.create().show();
        }
    };

    AdapterView.OnItemLongClickListener itemLongClickListenerParcela = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(final AdapterView<?> parent, View view, int position, long id) {
            try {
                final Parcela parcela = (Parcela) parent.getItemAtPosition(position);

                AlertDialog.Builder builder = new AlertDialog.Builder(ParcelasActivity.this);
                builder
                        .setTitle("Opções")
                        .setItems(R.array.dialog_options_parcela, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0: //Pagar

                                        new DialogFragment() {

                                            private AlertDialog dialog;
                                            private View view;

                                            private EditText edtDataPgto;


                                            @NonNull
                                            @Override
                                            public Dialog onCreateDialog(Bundle savedInstanceState) {
                                                super.onCreate(savedInstanceState);

                                                try {

                                                    LayoutInflater inflater = getActivity().getLayoutInflater();
                                                    view = inflater.inflate(R.layout.dialog_pagar, null);

                                                    //Referêncio os componentes visuais
                                                    edtDataPgto = (EditText) view.findViewById(R.id.edtDataPgto);

                                                    //Valores padrão
                                                    edtDataPgto.setText(DateUtil.formatDate(new Date()));

                                                    //Eventos
                                                    edtDataPgto.setOnClickListener(new ClickDate(ParcelasActivity.this.getSupportFragmentManager(),
                                                            edtDataPgto));

                                                    //Criando o dialog
                                                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                                    builder.setView(view)
                                                            .setTitle("Pagamento")
                                                            .setPositiveButton("Pagar", null)
                                                            .setNegativeButton("Cancelar", null);

                                                    dialog = builder.create();
                                                    dialog.show();

                                                    //Butão de inserção
                                                    dialog.getButton(Dialog.BUTTON_POSITIVE).setOnClickListener(clickPagar);


                                                } catch (Exception e) {
                                                    TreatException.treat(ParcelasActivity.this, e);
                                                }

                                                return dialog;
                                            }

                                            View.OnClickListener clickPagar = new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    parcela.setDataPagamento(DateUtil.parseDate(edtDataPgto.getText().toString()));
                                                    new PagarParcelasAsyncTask().execute(parcela);
                                                    dialog.dismiss();
                                                }
                                            };
                                        }.show(ParcelasActivity.this.getSupportFragmentManager(), "dialog");

                                        break;

                                }
                            }
                        })
                        .create().show();
            } catch (Exception e) {
                return false;
            }

            return false;
        }
    };

    RadioGroup.OnCheckedChangeListener checkedRadioGroupFiltro = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            filtrarParcelas(checkedId == R.id.rbParcelasTodas? -1 : (checkedId == R.id.rbParcelasPagar?
                    StatusParcela.pagar.ordinal() : StatusParcela.pago.ordinal()));
        }
    };


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

                return new ParcelaBo().getAllByEmprestimo(idEmprestimo);

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

                    parcelas = list;
                    filtrarParcelas(-1);
                }

            } else
                TreatException.treat(ParcelasActivity.this, exception);
        }
    }

    private void filtrarParcelas(int status) {

        List<Parcela> list;

        if (status == -1) {
            list = parcelas;
        } else {
            list = new ArrayList<>();
            for (Parcela parcela : parcelas) {
                if (parcela.getStatus() == status)
                    list.add(parcela);
            }
        }

        ListView lvParcelas = (ListView) ParcelasActivity.this.findViewById(R.id.lvParcelas);
        if (lvParcelas != null) lvParcelas.setAdapter(new AdapterListParcela(list));
    }

    private class PagarParcelasAsyncTask extends AsyncTask<Parcela, Void, Void > {
        private final ProgressDialog progressDialog;
        private Exception exception;

        public PagarParcelasAsyncTask() {
            progressDialog = new ProgressDialog(ParcelasActivity.this);
            progressDialog.setMessage("Buscando parcelas...");
            progressDialog.setCancelable(false);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
        }


        @Override
        protected Void doInBackground(Parcela... params) {
            try {

                new ParcelaBo().pagarParcela(params[0]);

            } catch (Exception e) {
                exception = e;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            progressDialog.dismiss();

            if (exception == null) {

                new BuscaParcelasAsyncTask().execute();

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

                viewHolder.tvDataParcela = (TextView) convertView.findViewById(R.id.tvNumeroParcela);
                viewHolder.tvValorParcela = (TextView) convertView.findViewById(R.id.tvValorParcela);
                viewHolder.tvStatusParcela = (TextView) convertView.findViewById(R.id.tvStatusParcela);

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            Parcela parcela = list.get(position);
            Double valorPagar = parcela.getValorPrincipal() + parcela.getValorJuros() + parcela.getValorMultaAtraso();

            viewHolder.tvDataParcela.setText(DateUtil.formatDate(parcela.getDataVencimento()));
            viewHolder.tvValorParcela.setText(NumberFormat.getCurrencyInstance().format(valorPagar));

            fillStatus(convertView, viewHolder, parcela);

            return convertView;
        }

        private class ViewHolder {
            TextView tvDataParcela;
            TextView tvValorParcela;
            TextView tvStatusParcela;
        }

        private void fillStatus(View convertView, ViewHolder viewHolder, Parcela parcela) {
            String status[] = {"A pagar", "Paga", "Em atraso"};
            int colors[] = {ContextCompat.getColor(ParcelasActivity.this, R.color.colorParcelaPagar),
                    ContextCompat.getColor(ParcelasActivity.this, R.color.colorParcelaPaga),
                    ContextCompat.getColor(ParcelasActivity.this, R.color.colorParcelaVencida)};

            convertView.setBackgroundColor(colors[parcela.getStatus()]);
            viewHolder.tvStatusParcela.setText(status[parcela.getStatus()]);
        }
    }
}
