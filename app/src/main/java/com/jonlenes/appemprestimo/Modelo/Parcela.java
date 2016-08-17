package com.jonlenes.appemprestimo.Modelo;

import java.util.Date;

/**
 * Created by Jonlenes on 11/08/2016.
 */

public class Parcela {

    private Long id;
    private Long idEmprestimo;
    private Long numero;
    private Date dataVencimento;
    private Double valorPrincipal;
    private Double valorJuros;
    private Double valorMultaAtraso;
    private Integer status;
    private Date dataPagamento;

    public Parcela(Long id, Long idEmprestimo, Long numero, Date dataVencimento,
                   Double valorPrincipal, Double valorJuros, Double valorMultaAtraso,
                   Integer status, Date dataPagamento) {
        this.id = id;
        this.idEmprestimo = idEmprestimo;
        this.numero = numero;
        this.dataVencimento = dataVencimento;
        this.valorPrincipal = valorPrincipal;
        this.valorJuros = valorJuros;
        this.valorMultaAtraso = valorMultaAtraso;
        this.status = status;
        this.dataPagamento = dataPagamento;
    }

    public Parcela(Long idEmprestimo, Long numero, Date dataVencimento, Double valorPrincipal, Double valorJuros, Integer status) {
        this.idEmprestimo = idEmprestimo;
        this.numero = numero;
        this.dataVencimento = dataVencimento;
        this.valorPrincipal = valorPrincipal;
        this.valorJuros = valorJuros;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIdEmprestimo() {
        return idEmprestimo;
    }

    public void setIdEmprestimo(Long idEmprestimo) {
        this.idEmprestimo = idEmprestimo;
    }

    public Long getNumero() {
        return numero;
    }

    public void setNumero(Long numero) {
        this.numero = numero;
    }

    public Date getDataVencimento() {
        return dataVencimento;
    }

    public void setDataVencimento(Date dataVencimento) {
        this.dataVencimento = dataVencimento;
    }

    public Double getValorPrincipal() {
        return valorPrincipal;
    }

    public void setValorPrincipal(Double valorPrincipal) {
        this.valorPrincipal = valorPrincipal;
    }

    public Double getValorJuros() {
        return valorJuros;
    }

    public void setValorJuros(Double valorJuros) {
        this.valorJuros = valorJuros;
    }

    public Double getValorMultaAtraso() {
        return valorMultaAtraso;
    }

    public void setValorMultaAtraso(Double valorMultaAtraso) {
        this.valorMultaAtraso = valorMultaAtraso;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Date getDataPagamento() {
        return dataPagamento;
    }

    public void setDataPagamento(Date dataPagamento) {
        this.dataPagamento = dataPagamento;
    }
}
