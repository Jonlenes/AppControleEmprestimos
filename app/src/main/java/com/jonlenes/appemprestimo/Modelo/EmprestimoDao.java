package com.jonlenes.appemprestimo.Modelo;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.jonlenes.appemprestimo.DbHelper;
import com.jonlenes.appemprestimo.Util;

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
        contentValues.put("data", Util.formatDateBd(emprestimo.getData()));
        contentValues.put("qtdeParcelas", emprestimo.getQtdeParcelas());
        contentValues.put("dataPrimeiraParcela", Util.formatDateBd(emprestimo.getDataPrimeiraParcela()));

        db.insert("Emprestimo", "id", contentValues);
    }

    public List<Emprestimo> getAll() {
        String sql = "SELECT id, descricao, valor, data, qtdeParcelas, " +
                "dataPrimeiraParcela FROM Emprestimo\n";

        Cursor cursor = db.rawQuery(sql, null);
        List<Emprestimo> emprestimos = new ArrayList<>();

        while (cursor.moveToNext())
            emprestimos.add(new Emprestimo(cursor.getLong(0),
                    cursor.getString(1),
                    cursor.getDouble(2),
                    Util.parseDateBd(cursor.getString(3)),
                    cursor.getLong(4),
                    Util.parseDateBd(cursor.getString(5))));

        return emprestimos;
    }

    public Long getMaxId() {
        String sql = "SELECT MAX(id) AS id FROM Emprestimo";

        Cursor cursor = db.rawQuery(sql, null);
        cursor.moveToNext();
        return cursor.getLong(0);
    }
}
