package com.medibuzz.data;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class MedicineDao_Impl implements MedicineDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<Medicine> __insertionAdapterOfMedicine;

  private final Converters __converters = new Converters();

  private final EntityDeletionOrUpdateAdapter<Medicine> __updateAdapterOfMedicine;

  public MedicineDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfMedicine = new EntityInsertionAdapter<Medicine>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `medicines` (`id`,`name`,`doseNote`,`hour`,`minute`,`scheduleType`,`startDate`,`isActive`,`createdAt`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Medicine entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindString(3, entity.getDoseNote());
        statement.bindLong(4, entity.getHour());
        statement.bindLong(5, entity.getMinute());
        final String _tmp = __converters.fromScheduleType(entity.getScheduleType());
        statement.bindString(6, _tmp);
        statement.bindLong(7, entity.getStartDate());
        final int _tmp_1 = entity.isActive() ? 1 : 0;
        statement.bindLong(8, _tmp_1);
        statement.bindLong(9, entity.getCreatedAt());
      }
    };
    this.__updateAdapterOfMedicine = new EntityDeletionOrUpdateAdapter<Medicine>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `medicines` SET `id` = ?,`name` = ?,`doseNote` = ?,`hour` = ?,`minute` = ?,`scheduleType` = ?,`startDate` = ?,`isActive` = ?,`createdAt` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Medicine entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindString(3, entity.getDoseNote());
        statement.bindLong(4, entity.getHour());
        statement.bindLong(5, entity.getMinute());
        final String _tmp = __converters.fromScheduleType(entity.getScheduleType());
        statement.bindString(6, _tmp);
        statement.bindLong(7, entity.getStartDate());
        final int _tmp_1 = entity.isActive() ? 1 : 0;
        statement.bindLong(8, _tmp_1);
        statement.bindLong(9, entity.getCreatedAt());
        statement.bindLong(10, entity.getId());
      }
    };
  }

  @Override
  public Object insert(final Medicine medicine, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfMedicine.insertAndReturnId(medicine);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object update(final Medicine medicine, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfMedicine.handle(medicine);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object getAllActive(final Continuation<? super List<Medicine>> $completion) {
    final String _sql = "SELECT * FROM medicines WHERE isActive = 1 ORDER BY hour, minute";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<Medicine>>() {
      @Override
      @NonNull
      public List<Medicine> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfDoseNote = CursorUtil.getColumnIndexOrThrow(_cursor, "doseNote");
          final int _cursorIndexOfHour = CursorUtil.getColumnIndexOrThrow(_cursor, "hour");
          final int _cursorIndexOfMinute = CursorUtil.getColumnIndexOrThrow(_cursor, "minute");
          final int _cursorIndexOfScheduleType = CursorUtil.getColumnIndexOrThrow(_cursor, "scheduleType");
          final int _cursorIndexOfStartDate = CursorUtil.getColumnIndexOrThrow(_cursor, "startDate");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<Medicine> _result = new ArrayList<Medicine>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Medicine _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpDoseNote;
            _tmpDoseNote = _cursor.getString(_cursorIndexOfDoseNote);
            final int _tmpHour;
            _tmpHour = _cursor.getInt(_cursorIndexOfHour);
            final int _tmpMinute;
            _tmpMinute = _cursor.getInt(_cursorIndexOfMinute);
            final ScheduleType _tmpScheduleType;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfScheduleType);
            _tmpScheduleType = __converters.toScheduleType(_tmp);
            final long _tmpStartDate;
            _tmpStartDate = _cursor.getLong(_cursorIndexOfStartDate);
            final boolean _tmpIsActive;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp_1 != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new Medicine(_tmpId,_tmpName,_tmpDoseNote,_tmpHour,_tmpMinute,_tmpScheduleType,_tmpStartDate,_tmpIsActive,_tmpCreatedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getById(final long id, final Continuation<? super Medicine> $completion) {
    final String _sql = "SELECT * FROM medicines WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Medicine>() {
      @Override
      @Nullable
      public Medicine call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfDoseNote = CursorUtil.getColumnIndexOrThrow(_cursor, "doseNote");
          final int _cursorIndexOfHour = CursorUtil.getColumnIndexOrThrow(_cursor, "hour");
          final int _cursorIndexOfMinute = CursorUtil.getColumnIndexOrThrow(_cursor, "minute");
          final int _cursorIndexOfScheduleType = CursorUtil.getColumnIndexOrThrow(_cursor, "scheduleType");
          final int _cursorIndexOfStartDate = CursorUtil.getColumnIndexOrThrow(_cursor, "startDate");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final Medicine _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpDoseNote;
            _tmpDoseNote = _cursor.getString(_cursorIndexOfDoseNote);
            final int _tmpHour;
            _tmpHour = _cursor.getInt(_cursorIndexOfHour);
            final int _tmpMinute;
            _tmpMinute = _cursor.getInt(_cursorIndexOfMinute);
            final ScheduleType _tmpScheduleType;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfScheduleType);
            _tmpScheduleType = __converters.toScheduleType(_tmp);
            final long _tmpStartDate;
            _tmpStartDate = _cursor.getLong(_cursorIndexOfStartDate);
            final boolean _tmpIsActive;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp_1 != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _result = new Medicine(_tmpId,_tmpName,_tmpDoseNote,_tmpHour,_tmpMinute,_tmpScheduleType,_tmpStartDate,_tmpIsActive,_tmpCreatedAt);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getAll(final Continuation<? super List<Medicine>> $completion) {
    final String _sql = "SELECT * FROM medicines ORDER BY createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<Medicine>>() {
      @Override
      @NonNull
      public List<Medicine> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfDoseNote = CursorUtil.getColumnIndexOrThrow(_cursor, "doseNote");
          final int _cursorIndexOfHour = CursorUtil.getColumnIndexOrThrow(_cursor, "hour");
          final int _cursorIndexOfMinute = CursorUtil.getColumnIndexOrThrow(_cursor, "minute");
          final int _cursorIndexOfScheduleType = CursorUtil.getColumnIndexOrThrow(_cursor, "scheduleType");
          final int _cursorIndexOfStartDate = CursorUtil.getColumnIndexOrThrow(_cursor, "startDate");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<Medicine> _result = new ArrayList<Medicine>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Medicine _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpDoseNote;
            _tmpDoseNote = _cursor.getString(_cursorIndexOfDoseNote);
            final int _tmpHour;
            _tmpHour = _cursor.getInt(_cursorIndexOfHour);
            final int _tmpMinute;
            _tmpMinute = _cursor.getInt(_cursorIndexOfMinute);
            final ScheduleType _tmpScheduleType;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfScheduleType);
            _tmpScheduleType = __converters.toScheduleType(_tmp);
            final long _tmpStartDate;
            _tmpStartDate = _cursor.getLong(_cursorIndexOfStartDate);
            final boolean _tmpIsActive;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp_1 != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new Medicine(_tmpId,_tmpName,_tmpDoseNote,_tmpHour,_tmpMinute,_tmpScheduleType,_tmpStartDate,_tmpIsActive,_tmpCreatedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
