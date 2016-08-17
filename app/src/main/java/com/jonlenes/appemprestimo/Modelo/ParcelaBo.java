package com.jonlenes.appemprestimo.Modelo;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by Jonlenes on 11/08/2016.
 */

public class ParcelaBo {

    private void calculaValorParcelaHoje(Parcela parcela) {
        Long diffDay = TimeUnit.DAYS.convert(parcela.getDataPagamento().getTime() - new Date().getTime(), TimeUnit.MILLISECONDS);
        if (diffDay > 0)
            parcela.setValorMultaAtraso(parcela.getValorPrincipal() * diffDay * 0.005);
        else if (Math.abs(diffDay) >= 30)
            parcela.setValorJuros(parcela.getValorJuros() / 2);
    }
}
