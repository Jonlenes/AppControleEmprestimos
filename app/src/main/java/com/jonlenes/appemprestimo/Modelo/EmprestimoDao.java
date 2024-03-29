package com.jonlenes.appemprestimo.Modelo;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.jonlenes.appemprestimo.Geral.DateUtil;
import com.jonlenes.appemprestimo.DbHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Jonlenes on 11/08/2016.
 */

public class EmprestimoDao {
    private SQLiteDatabase db;

    public EmprestimoDao() {
        this.db = DbHelper.getInstance().getWritableDatabase();
    }

    public void insert(Emprestimo emprestimo) {

        ContentValues contentValues = new ContentValues();

        contentValues.put("descricao", emprestimo.getDescricao());
        contentValues.put("valor", emprestimo.getValor());
        contentValues.put("data", DateUtil.formatDateBd(emprestimo.getData()));
        contentValues.put("qtdeParcelas", emprestimo.getQtdeParcelas());
        contentValues.put("dataPrimeiraParcela", DateUtil.formatDateBd(emprestimo.getDataPrimeiraParcela()));

        db.insert("Emprestimo", "id", contentValues);
    }

    public void update(Emprestimo emprestimo) {

        ContentValues contentValues = new ContentValues();

        contentValues.put("descricao", emprestimo.getDescricao());
        contentValues.put("valor", emprestimo.getValor());
        contentValues.put("data", DateUtil.formatDateBd(emprestimo.getData()));
        contentValues.put("qtdeParcelas", emprestimo.getQtdeParcelas());
        contentValues.put("dataPrimeiraParcela", DateUtil.formatDateBd(emprestimo.getDataPrimeiraParcela()));

        db.update("Emprestimo", contentValues, "id = " + emprestimo.getId(), null);
    }

    public void delete(Long id) {
        db.delete("Emprestimo", "id = " + id, null);
    }

    public List<Emprestimo> getAll() {
        String sql = "SELECT Emprestimo.id, Emprestimo.descricao, Emprestimo.valor, Emprestimo.data, \n" +
                "       Emprestimo.qtdeParcelas, Emprestimo.dataPrimeiraParcela,\n" +
                "       COUNT(Parcela.id) AS qtdePagar\n" +
                "FROM Emprestimo\n" +
                "LEFT JOIN Parcela\n" +
                "   ON Parcela.idEmprestimo = Emprestimo.id\n" +
                "GROUP BY Emprestimo.id, Emprestimo.descricao, Emprestimo.valor, Emprestimo.data, \n" +
                "   Emprestimo.qtdeParcelas, Emprestimo.dataPrimeiraParcela";

        Cursor cursor = db.rawQuery(sql, null);
        List<Emprestimo> emprestimos = new ArrayList<>();

        while (cursor.moveToNext())
            emprestimos.add(new Emprestimo(cursor.getLong(0),
                    cursor.getString(1),
                    cursor.getDouble(2),
                    DateUtil.parseDateBd(cursor.getString(3)),
                    cursor.getLong(4),
                    DateUtil.parseDateBd(cursor.getString(5)),
                    cursor.getInt(6)));

        return emprestimos;
    }

    public Emprestimo getById(Long id) {
        String sql = "SELECT id, descricao, valor, data, qtdeParcelas, dataPrimeiraParcela\n" +
                "FROM Emprestimo\n" +
                "WHERE id = " + id;

        Cursor cursor = db.rawQuery(sql, null);

        if (cursor.moveToNext())
            return new Emprestimo(cursor.getLong(0),
                    cursor.getString(1),
                    cursor.getDouble(2),
                    DateUtil.parseDateBd(cursor.getString(3)),
                    cursor.getLong(4),
                    DateUtil.parseDateBd(cursor.getString(5)));

        return null;
    }

    public Long getMaxId() {
        String sql = "SELECT MAX(id) AS id FROM Emprestimo";

        Cursor cursor = db.rawQuery(sql, null);
        cursor.moveToNext();
        return cursor.getLong(0);
    }

    public List<Emprestimo> getEmprestimoComParcela(Date dateInicial, Date dateFinal, Integer status) {
        String sql = "SELECT Emprestimo.descricao, Emprestimo.qtdeParcelas, \n" +
                "       Parcela.id, Parcela.idEmprestimo, Parcela.numero, Parcela.dataVencimento,\n" +
                "       Parcela.valorPrincipal, Parcela.valorJuros, Parcela.valorMultaAtraso,\n" +
                "       Parcela.status, Parcela.dataPagamento\n" +
                "FROM   Emprestimo\n" +
                "INNER JOIN Parcela\n" +
                "   ON  Emprestimo.id = Parcela.idEmprestimo\n" +
                "WHERE  Parcela.dataVencimento BETWEEN '" + DateUtil.formatDateBd(dateInicial) + "'\n" +
                "       AND '" + DateUtil.formatDateBd(dateFinal) + "'\n" +
                "       AND Parcela.status = " + status + "\n" +
                "ORDER BY\n" +
                "       Parcela.dataVencimento,\n" +
                "       Emprestimo.id";

        Cursor cursor = db.rawQuery(sql, null);
        List<Emprestimo> list = new ArrayList<>();

        while (cursor.moveToNext())
            list.add(new Emprestimo(cursor.getString(0), cursor.getLong(1),
                    new Parcela(cursor.getLong(2),
                            cursor.getLong(3),
                            cursor.getLong(4),
                            DateUtil.parseDateBd(cursor.getString(5)),
                            cursor.getDouble(6),
                            cursor.getDouble(7),
                            cursor.getDouble(8),
                            cursor.getInt(9),
                            !cursor.isNull(10)? DateUtil.parseDateBd(cursor.getString(10)) : null)));

        return list;
    }
}
