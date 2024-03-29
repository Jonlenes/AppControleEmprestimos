package com.jonlenes.appemprestimo.Modelo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Jonlenes on 11/08/2016.
 */

public class Emprestimo {

    private Long id;
    private String descricao;
    private Double valor;
    private Date data;
    private Long qtdeParcelas;
    private Date dataPrimeiraParcela;
    private Integer qtdeParcelasPagar;
    private List<Parcela> parcelas;

    public Emprestimo(Long id, String descricao, Double valor, Date data, Long qtdeParcelas, Date dataPrimeiraParcela, Integer qtdeParcelasPagar) {
        this.id = id;
        this.descricao = descricao;
        this.valor = valor;
        this.data = data;
        this.qtdeParcelas = qtdeParcelas;
        this.dataPrimeiraParcela = dataPrimeiraParcela;
        this.qtdeParcelasPagar = qtdeParcelasPagar;
    }

    public Emprestimo(Long id, String descricao, Double valor, Date data, Long qtdeParcelas, Date dataPrimeiraParcela) {
        this.id = id;
        this.descricao = descricao;
        this.valor = valor;
        this.data = data;
        this.qtdeParcelas = qtdeParcelas;
        this.dataPrimeiraParcela = dataPrimeiraParcela;
    }

    public Emprestimo(String descricao, Double valor, Date data,
                      Long qtdeParcelas, Date dataPrimeiraParcela) {
        this.descricao = descricao;
        this.valor = valor;
        this.data = data;
        this.qtdeParcelas = qtdeParcelas;
        this.dataPrimeiraParcela = dataPrimeiraParcela;
    }

    public Emprestimo(String descricao, Long qtdeParcelas, Parcela parcela) {
        this.descricao = descricao;
        this.qtdeParcelas = qtdeParcelas;

        parcelas = new ArrayList<>();
        parcelas.add(parcela);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Double getValor() {
        return valor;
    }

    public void setValor(Double valor) {
        this.valor = valor;
    }

    public Date getData() {
        return data;
    }

    public void setData(Date data) {
        this.data = data;
    }

    public Long getQtdeParcelas() {
        return qtdeParcelas;
    }

    public void setQtdeParcelas(Long qtdeParcelas) {
        this.qtdeParcelas = qtdeParcelas;
    }

    public Date getDataPrimeiraParcela() {
        return dataPrimeiraParcela;
    }

    public void setDataPrimeiraParcela(Date dataPrimeiraParcela) {
        this.dataPrimeiraParcela = dataPrimeiraParcela;
    }

    public Integer getQtdeParcelasPagar() {
        return qtdeParcelasPagar;
    }

    public void setQtdeParcelasPagar(Integer qtdeParcelasPagar) {
        this.qtdeParcelasPagar = qtdeParcelasPagar;
    }

    public List<Parcela> getParcelas() {
        return parcelas;
    }

    public void setParcelas(List<Parcela> parcelas) {
        this.parcelas = parcelas;
    }
}
