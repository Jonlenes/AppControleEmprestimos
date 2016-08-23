package com.jonlenes.appemprestimo.Modelo;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Jonlenes on 11/08/2016.
 */

public class EmprestimoBo {
    public void insertOrUpdate(Emprestimo emprestimo) {

        ParcelaDao parcelaDao = new ParcelaDao();
        EmprestimoDao emprestimoDao = new EmprestimoDao();

        if (emprestimo.getId() == -1) {
            emprestimoDao.insert(emprestimo);
            emprestimo.setId(emprestimoDao.getMaxId());
        } else {

            List<Parcela> parcelas = parcelaDao.getAllByEmprestimo(emprestimo.getId());
            for (Parcela parcela : parcelas) {
                if (parcela.getStatus() == StatusParcela.pago.ordinal())
                    throw new RuntimeException("Não é possível atualizar o empréstimo, " +
                            "pois ele possui parcelas pagas.");
            }

            parcelaDao.deleteByEmprestimo(emprestimo.getId());
            emprestimoDao.update(emprestimo);
        }


        Long diasAntesPrimeiraParcela = TimeUnit.DAYS.convert(emprestimo.getDataPrimeiraParcela().getTime()
                - emprestimo.getData().getTime(), TimeUnit.MILLISECONDS);

        if (diasAntesPrimeiraParcela < 10 && emprestimo.getQtdeParcelas() == 1)

            parcelaDao.insert(new Parcela(emprestimo.getId(), 1L, emprestimo.getDataPrimeiraParcela(),
                    emprestimo.getValor(), emprestimo.getValor() * 0.05, 0));

        else {

            Double valorPricipalParcela = emprestimo.getValor() / emprestimo.getQtdeParcelas();
            Double valorJurosParcela = (emprestimo.getValor() * ((((diasAntesPrimeiraParcela / 30)
                    + emprestimo.getQtdeParcelas()) + 10 - 1) / 100.0)) / emprestimo.getQtdeParcelas();

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(emprestimo.getDataPrimeiraParcela());

            for (int i = 0; i < emprestimo.getQtdeParcelas(); ++i) {

                parcelaDao.insert(new Parcela(emprestimo.getId(),
                        (long) (i + 1),
                        calendar.getTime(),
                        valorPricipalParcela,
                        valorJurosParcela,
                        StatusParcela.pagar.ordinal()));

                calendar.add(Calendar.MONTH, 1);
            }
        }
    }

    public void delete(Long id) {
        new ParcelaDao().deleteByEmprestimo(id);
        new EmprestimoDao().delete(id);
    }
}
