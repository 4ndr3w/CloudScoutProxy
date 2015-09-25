package com.example.andrew.cloudscoutproxy;

import com.orm.SugarRecord;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.Iterator;

/**
 * Created by andrew on 7/15/15.
 */
public class CachedReport extends SugarRecord<CachedReport> {
    String report;

    public CachedReport()
    {

    }

    public CachedReport(JSONArray report)
    {
        this.report = report.toString();
    }

    public JSONArray getReport() throws JSONException
    {
        return new JSONArray(report);
    }

    public static void deleteAll()
    {
        deleteAll(CachedReport.class);
    }

    public static long count()
    {
        return count(CachedReport.class, "", new String[]{});
    }

    public static Iterator<CachedReport> findAll()
    {
        return CachedReport.findAll(CachedReport.class);
    }
}
