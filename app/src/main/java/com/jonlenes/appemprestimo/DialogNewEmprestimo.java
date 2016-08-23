package com.jonlenes.appemprestimo;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.jonlenes.appemprestimo.Geral.ClickDate;
import com.jonlenes.appemprestimo.Geral.DateUtil;
import com.jonlenes.appemprestimo.Modelo.Emprestimo;
import com.jonlenes.appemprestimo.Modelo.EmprestimoBo;
import com.jonlenes.appemprestimo.Modelo.EmprestimoDao;

import java.util.Date;

/**
 * Created by Jonlenes on 23/08/2016.
 */
public class DialogNewEmprestimo extends DialogFragment {

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
            edtDataEmp.setText(DateUtil.formatDate(new Date()));
            edtDatePrimeiraParcela.setText(DateUtil.formatDate(new Date()));
            //edtValorEmp.setText(NumberFormat.getCurrencyInstance().format(0));

            //Eventos
            edtDataEmp.setOnClickListener(new ClickDate(getActivity().getSupportFragmentManager(),
                    edtDataEmp));
            edtDatePrimeiraParcela.setOnClickListener(new ClickDate(getActivity().getSupportFragmentManager(),
                    edtDatePrimeiraParcela));
            //edtValorEmp.addTextChangedListener(textWatcherValor);

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

            if (idEmprestimo != -1)
                new BuscaEmprestimoAsyncTask().execute();

        } catch (Exception e) {
            TreatException.treat(getActivity(), e);
        }

        return dialog;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

        ((MainActivity) getActivity()).atualizaEmprestimos();
    }

    View.OnClickListener clickListenerInserir = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            try {

                if (validFields()) {

                    new InsertEmprestimoAsyncTask().execute(new Emprestimo(idEmprestimo,
                            edtDescricaoEmp.getText().toString(),
                            Double.parseDouble(edtValorEmp.getText().toString()),
                            DateUtil.parseDate(edtDataEmp.getText().toString()),
                            Long.parseLong(edtQtdeParcelas.getText().toString()),
                            DateUtil.parseDate(edtDatePrimeiraParcela.getText().toString())));
                }

            } catch (Exception e) {
                TreatException.treat(getActivity(), e);
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
            progressDialog = new ProgressDialog(getActivity());
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

                new EmprestimoBo().insertOrUpdate(params[0]);

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
                TreatException.treat(getActivity(), exception);
            else {
                dialog.dismiss();
            }
        }
    }


    class BuscaEmprestimoAsyncTask extends AsyncTask<Void, Void, Emprestimo> {
        private ProgressDialog progressDialog;
        private Exception exception;

        public BuscaEmprestimoAsyncTask() {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("Buscando empréstimo...");
            progressDialog.setCancelable(false);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
        }

        @Override
        protected Emprestimo doInBackground(Void... params) {
            try {

                return new EmprestimoDao().getById(idEmprestimo);

            } catch (Exception e) {
                exception = e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Emprestimo emprestimo) {
            super.onPostExecute(emprestimo);

            progressDialog.dismiss();

            if (exception == null) {

                edtDescricaoEmp.setText(emprestimo.getDescricao());
                edtValorEmp.setText(String.valueOf(emprestimo.getValor()));
                edtDataEmp.setText(DateUtil.formatDate(emprestimo.getData()));
                edtQtdeParcelas.setText(String.valueOf(emprestimo.getQtdeParcelas()));
                edtDatePrimeiraParcela.setText(DateUtil.formatDate(emprestimo.getDataPrimeiraParcela()));

            } else {
                TreatException.treat(getActivity(), exception);
            }

        }
    }
}
