package com.jonlenes.appemprestimo.Modelo;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.jonlenes.appemprestimo.Geral.DateUtil;
import com.jonlenes.appemprestimo.DbHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by Jonlenes on 15/08/2016.
 */

public class ParcelaDao {
    private SQLiteDatabase db;

    public ParcelaDao() {
        this.db = DbHelper.getInstance().getWritableDatabase();
    }

    public void insert(Parcela parcela) {

        ContentValues contentValues = new ContentValues();

        contentValues.put("idEmprestimo", parcela.getIdEmprestimo());
        contentValues.put("numero", parcela.getNumero());
        contentValues.put("dataVencimento", DateUtil.formatDateBd(parcela.getDataVencimento()));
        contentValues.put("valorPrincipal", parcela.getValorPrincipal());
        contentValues.put("valorJuros", parcela.getValorJuros());
        contentValues.put("valorMultaAtraso", parcela.getValorMultaAtraso());
        contentValues.put("status", parcela.getStatus());
        contentValues.put("dataPagamento",  DateUtil.formatDateBd(parcela.getDataPagamento()));

        db.insert("Parcela", "id", contentValues);
    }

    public void update(Parcela parcela) {

        ContentValues contentValues = new ContentValues();

        contentValues.put("valorMultaAtraso", parcela.getValorMultaAtraso());
        contentValues.put("valorJuros", parcela.getValorJuros());
        contentValues.put("status", parcela.getStatus());
        contentValues.put("dataPagamento",  DateUtil.formatDateBd(parcela.getDataPagamento()));

        db.update("Parcela", contentValues, "id = " + parcela.getId(), null);
    }

    public void deleteByEmprestimo(Long idEmprestimo) {
        db.delete("Parcela", "idEmprestimo = " + idEmprestimo, null);
    }

    public List<Parcela> getAllByEmprestimo(Long idEmprestimo) {
        String sql = "SELECT id, idEmprestimo, numero, dataVencimento, valorPrincipal, \n" +
                "valorJuros, valorMultaAtraso, status, dataPagamento FROM Parcela\n" +
                "WHERE idEmprestimo = " + idEmprestimo;

        Cursor cursor = db.rawQuery(sql, null);
        List<Parcela> parcelas = new ArrayList<>();

        while (cursor.moveToNext())
            parcelas.add(new Parcela(cursor.getLong(0),
                    cursor.getLong(1),
                    cursor.getLong(2),
                    DateUtil.parseDateBd(cursor.getString(3)),
                    cursor.getDouble(4),
                    cursor.getDouble(5),
                    cursor.getDouble(6),
                    cursor.getInt(7),
                    !cursor.isNull(8)? DateUtil.parseDateBd(cursor.getString(8)) : null));

        return parcelas;
    }

    public Map<Emprestimo, Parcela> getParcelas(Date dateInicial, Date dateFinal, Integer status) {
        String sql = "SELECT Emprestimo.descricao, Emprestimo.qtdeParcelas, Emprestimo.descricao,\n" +
                "       Parcela.id, Parcela.idEmprestimo, Parcela.numero, arcela.dataVencimento,\n" +
                "       Parcela.valorPrincipal, Parcela.valorJuros, Parcela.valorMultaAtraso,\n" +
                "       Parcela.status, Parcela.dataPagamento\n" +
                "FROM   Parcela\n" +
                "INNER JOIN Emprestimo\n" +
                "   ON  Emprestimo.id = Parcela.idEmprestimo\n" +
                "WHERE  Parcela.dataVencimento BETWEEN 'aaaaa' AND 'bbbbbbb'\n" +
                "       AND Parcela.status\n" +
                "ORDER BY\n" +
                "       Parcela.dataVencimento,\n" +
                "       Emprestimo.id" + ;

        Cursor cursor = db.rawQuery(sql, null);
        List<Parcela> parcelas = new ArrayList<>();

        while (cursor.moveToNext())
            parcelas.add(new com.jonlenes.appemprestimo.Modelo.Parcela(cursor.getLong(0),
                    cursor.getLong(1),
                    cursor.getLong(2),
                    DateUtil.parseDateBd(cursor.getString(3)),
                    cursor.getDouble(4),
                    cursor.getDouble(5),
                    cursor.getDouble(6),
                    cursor.getInt(7),
                    !cursor.isNull(8)? DateUtil.parseDateBd(cursor.getString(8)) : null));

        return parcelas;
    }
}
