package com.readyandroid.database;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AppSQLite {
    private HashMap<String, String> addColumns;
    private ArrayList<String> removeColumns;
    private HashMap<String, String> renameColumn;
    private SQLiteDatabase db;
    private String tableName;
    private String toTableName;

    public AppSQLite(SQLiteDatabase db, String tableName) {
        this.db = db;
        this.tableName = tableName;
        addColumns = new HashMap<>();
        removeColumns = new ArrayList<>();
        renameColumn = new HashMap<>();
    }

    public void addColumn(String columnName, String dataType) {
        addColumns.put(columnName, dataType);
    }

    public void renameColumn(String fromColumnName, String toColumnName) {
        renameColumn.put(fromColumnName, toColumnName);
    }

    public void removeColumns(String columnName) {
        removeColumns.add(columnName);
    }

    public void renameTableName(String toTableName) {
        this.toTableName = toTableName;
    }

    public void execute() {
        ArrayList<ColumnInfo> columnInfoArrayList = null;
        //Rename table
        if (!TextUtils.isEmpty(toTableName)) {
            try {
                db.execSQL("ALTER TABLE " + tableName + " RENAME TO " + toTableName);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            tableName = toTableName;
        }

        //Add column
        if (addColumns != null && !addColumns.isEmpty()) {
            for (Map.Entry<String, String> stringStringEntry : addColumns.entrySet()) {
                String columnName = stringStringEntry.getKey();
                String columnType = stringStringEntry.getValue();
                try {
                    db.execSQL("ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + columnType + "");
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        //Remove Column
        if (removeColumns != null && !removeColumns.isEmpty()) {
            try {
                columnInfoArrayList = getAllColumns(tableName);
                String allColumnsExceptRemoved = "*";
                StringBuilder columnNames = new StringBuilder();
                //reading all column names
                for (String removeColumn : removeColumns) {
                    for (ColumnInfo columnInfo : columnInfoArrayList) {
                        if (!columnInfo.name.equals(removeColumn)) {
                            columnNames.append(columnInfo.name);
                            columnNames.append(",");
                        }
                    }
                }
                allColumnsExceptRemoved = columnNames.toString().replaceAll(",$", "");

                db.execSQL("ALTER TABLE " + tableName + " RENAME TO " + tableName + "_back");
                db.execSQL("CREATE TABLE " + tableName + " AS SELECT " + allColumnsExceptRemoved + " FROM " + tableName + "_back WHERE 1");
                db.execSQL("DROP TABLE " + tableName + "_back");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        //Rename column
        if (renameColumn != null && !renameColumn.isEmpty()) {
            try {
                if (columnInfoArrayList == null) {
                    columnInfoArrayList = getAllColumns(tableName);
                }

                String createTableStatement = "CREATE TABLE " + tableName + "(";

                String fromColumns = "";
                String toColumns = "";

                for (ColumnInfo columnInfo : columnInfoArrayList) {

                    String from = columnInfo.name;
                    String to = columnInfo.name ;
                    String datatype = columnInfo.datatype;
                    for (Map.Entry<String, String> stringStringEntry : renameColumn.entrySet()) {

                        String fromColumnName = stringStringEntry.getKey();
                        String toColumnName = stringStringEntry.getValue();

                        if (columnInfo.name.equals(fromColumnName)) {
                            from = columnInfo.name;
                            to = toColumnName;
                            datatype = columnInfo.datatype;
                            break;
                        }
                    }
                    fromColumns += from + ",";
                    toColumns += to + ",";
                    createTableStatement += to + " " + datatype + ",";

                }
                createTableStatement += ")";

                createTableStatement = createTableStatement.replace(",)", ")");
                fromColumns = fromColumns.replaceAll(",$", "");
                toColumns = toColumns.replaceAll(",$", "");

                db.execSQL("ALTER TABLE " + tableName + " RENAME TO " + tableName + "_back");
                db.execSQL(createTableStatement);
                db.execSQL("INSERT INTO " + tableName + "(" + toColumns + ") SELECT " + fromColumns + " FROM " + tableName + "_back");
                db.execSQL("DROP TABLE " + tableName + "_back");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    ArrayList<ColumnInfo> getAllColumns(String tableName) {
        ArrayList<ColumnInfo> columnInfoList = new ArrayList<>();
        Cursor cursor = db.rawQuery("PRAGMA table_info(" + tableName + ")", null);
        while (cursor.moveToNext()) {
            String name = cursor.getString(cursor.getColumnIndex("name"));
            String type = cursor.getString(cursor.getColumnIndex("type"));
            String default_value = cursor.getString(cursor.getColumnIndex("dflt_value"));
            String notnull = cursor.getString(cursor.getColumnIndex("notnull"));
            String pk = cursor.getString(cursor.getColumnIndex("pk"));

            ColumnInfo columnInfo = new ColumnInfo();
            columnInfo.name = name;
            columnInfo.datatype = type;
            columnInfo.defaultValue = default_value;
            columnInfo.isNotNull = "1".equals(notnull) ? true : false;
            columnInfo.isPrimaryKey = "1".equals(pk) ? true : false;

            columnInfoList.add(columnInfo);
        }
//        Cursor cursor = db.rawQuery("SELECT * FROM " + tableName + " LIMIT 0", null);
        return columnInfoList;
    }
}