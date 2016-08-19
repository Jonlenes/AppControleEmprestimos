package com.jonlenes.appemprestimo.Modelo;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Jonlenes on 11/08/2016.
 */

public class ParcelaBo {

    private Double calculaMulta(Parcela parcela) {
        return parcela.getValorPrincipal() * (TimeUnit.DAYS.convert(new Date().getTime() -
                parcela.getDataVencimento().getTime(), TimeUnit.MILLISECONDS)) * 0.005;
    }

    public List<Parcela> getAllByEmprestimo(Long idEmprestimo) {
        List<Parcela> parcelas = new ParcelaDao().getAllByEmprestimo(idEmprestimo);

        for (Parcela parcela : parcelas) {
            if (parcela.getStatus() != StatusParcela.pago.ordinal() &&
                    ((TimeUnit.DAYS.convert(parcela.getDataVencimento().getTime() -
                                            new Date().getTime(), TimeUnit.MILLISECONDS)) < 0) ) {
                parcela.setStatus(StatusParcela.atrasada.ordinal());
                parcela.setValorMultaAtraso(calculaMulta(parcela));
            }
        }

        return parcelas;
    }

    public void pagarParcela(Parcela parcela) {

        parcela.setDataPagamento(new Date());
        parcela.setStatus(StatusParcela.pago.ordinal());
        new ParcelaDao().update(parcela);
    }

}
