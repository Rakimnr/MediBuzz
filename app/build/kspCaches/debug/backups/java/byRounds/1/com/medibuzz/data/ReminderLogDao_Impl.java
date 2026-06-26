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
public final class ReminderLogDao_Impl implements ReminderLogDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<ReminderLog> __insertionAdapterOfReminderLog;

  private final Converters __converters = new Converters();

  private final EntityDeletionOrUpdateAdapter<ReminderLog> __updateAdapterOfReminderLog;

  public ReminderLogDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfReminderLog = new EntityInsertionAdapter<ReminderLog>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `reminder_logs` (`id`,`medicineId`,`medicineName`,`scheduledTime`,`status`,`confirmedTime`) VALUES (nullif(?, 0),?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ReminderLog entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getMedicineId());
        statement.bindString(3, entity.getMedicineName());
        statement.bindLong(4, entity.getScheduledTime());
        final String _tmp = __converters.fromReminderStatus(entity.getStatus());
        statement.bindString(5, _tmp);
        if (entity.getConfirmedTime() == null) {
          statement.bindNull(6);
        } else {
          statement.bindLong(6, entity.getConfirmedTime());
        }
      }
    };
    this.__updateAdapterOfReminderLog = new EntityDeletionOrUpdateAdapter<ReminderLog>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `reminder_logs` SET `id` = ?,`medicineId` = ?,`medicineName` = ?,`scheduledTime` = ?,`status` = ?,`confirmedTime` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ReminderLog entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getMedicineId());
        statement.bindString(3, entity.getMedicineName());
        statement.bindLong(4, entity.getScheduledTime());
        final String _tmp = __converters.fromReminderStatus(entity.getStatus());
        statement.bindString(5, _tmp);
        if (entity.getConfirmedTime() == null) {
          statement.bindNull(6);
        } else {
          statement.bindLong(6, entity.getConfirmedTime());
        }
        statement.bindLong(7, entity.getId());
      }
    };
  }

  @Override
  public Object insert(final ReminderLog log, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfReminderLog.insertAndReturnId(log);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object update(final ReminderLog log, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfReminderLog.handle(log);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object getAll(final Continuation<? super List<ReminderLog>> $completion) {
    final String _sql = "SELECT * FROM reminder_logs ORDER BY scheduledTime DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<ReminderLog>>() {
      @Override
      @NonNull
      public List<ReminderLog> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfMedicineId = CursorUtil.getColumnIndexOrThrow(_cursor, "medicineId");
          final int _cursorIndexOfMedicineName = CursorUtil.getColumnIndexOrThrow(_cursor, "medicineName");
          final int _cursorIndexOfScheduledTime = CursorUtil.getColumnIndexOrThrow(_cursor, "scheduledTime");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfConfirmedTime = CursorUtil.getColumnIndexOrThrow(_cursor, "confirmedTime");
          final List<ReminderLog> _result = new ArrayList<ReminderLog>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ReminderLog _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpMedicineId;
            _tmpMedicineId = _cursor.getLong(_cursorIndexOfMedicineId);
            final String _tmpMedicineName;
            _tmpMedicineName = _cursor.getString(_cursorIndexOfMedicineName);
            final long _tmpScheduledTime;
            _tmpScheduledTime = _cursor.getLong(_cursorIndexOfScheduledTime);
            final ReminderStatus _tmpStatus;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfStatus);
            _tmpStatus = __converters.toReminderStatus(_tmp);
            final Long _tmpConfirmedTime;
            if (_cursor.isNull(_cursorIndexOfConfirmedTime)) {
              _tmpConfirmedTime = null;
            } else {
              _tmpConfirmedTime = _cursor.getLong(_cursorIndexOfConfirmedTime);
            }
            _item = new ReminderLog(_tmpId,_tmpMedicineId,_tmpMedicineName,_tmpScheduledTime,_tmpStatus,_tmpConfirmedTime);
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
  public Object getById(final long id, final Continuation<? super ReminderLog> $completion) {
    final String _sql = "SELECT * FROM reminder_logs WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<ReminderLog>() {
      @Override
      @Nullable
      public ReminderLog call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfMedicineId = CursorUtil.getColumnIndexOrThrow(_cursor, "medicineId");
          final int _cursorIndexOfMedicineName = CursorUtil.getColumnIndexOrThrow(_cursor, "medicineName");
          final int _cursorIndexOfScheduledTime = CursorUtil.getColumnIndexOrThrow(_cursor, "scheduledTime");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfConfirmedTime = CursorUtil.getColumnIndexOrThrow(_cursor, "confirmedTime");
          final ReminderLog _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpMedicineId;
            _tmpMedicineId = _cursor.getLong(_cursorIndexOfMedicineId);
            final String _tmpMedicineName;
            _tmpMedicineName = _cursor.getString(_cursorIndexOfMedicineName);
            final long _tmpScheduledTime;
            _tmpScheduledTime = _cursor.getLong(_cursorIndexOfScheduledTime);
            final ReminderStatus _tmpStatus;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfStatus);
            _tmpStatus = __converters.toReminderStatus(_tmp);
            final Long _tmpConfirmedTime;
            if (_cursor.isNull(_cursorIndexOfConfirmedTime)) {
              _tmpConfirmedTime = null;
            } else {
              _tmpConfirmedTime = _cursor.getLong(_cursorIndexOfConfirmedTime);
            }
            _result = new ReminderLog(_tmpId,_tmpMedicineId,_tmpMedicineName,_tmpScheduledTime,_tmpStatus,_tmpConfirmedTime);
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
  public Object getByMedicineAndScheduledTime(final long medicineId, final long scheduledTime,
      final Continuation<? super ReminderLog> $completion) {
    final String _sql = "SELECT * FROM reminder_logs WHERE medicineId = ? AND scheduledTime = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, medicineId);
    _argIndex = 2;
    _statement.bindLong(_argIndex, scheduledTime);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<ReminderLog>() {
      @Override
      @Nullable
      public ReminderLog call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfMedicineId = CursorUtil.getColumnIndexOrThrow(_cursor, "medicineId");
          final int _cursorIndexOfMedicineName = CursorUtil.getColumnIndexOrThrow(_cursor, "medicineName");
          final int _cursorIndexOfScheduledTime = CursorUtil.getColumnIndexOrThrow(_cursor, "scheduledTime");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfConfirmedTime = CursorUtil.getColumnIndexOrThrow(_cursor, "confirmedTime");
          final ReminderLog _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpMedicineId;
            _tmpMedicineId = _cursor.getLong(_cursorIndexOfMedicineId);
            final String _tmpMedicineName;
            _tmpMedicineName = _cursor.getString(_cursorIndexOfMedicineName);
            final long _tmpScheduledTime;
            _tmpScheduledTime = _cursor.getLong(_cursorIndexOfScheduledTime);
            final ReminderStatus _tmpStatus;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfStatus);
            _tmpStatus = __converters.toReminderStatus(_tmp);
            final Long _tmpConfirmedTime;
            if (_cursor.isNull(_cursorIndexOfConfirmedTime)) {
              _tmpConfirmedTime = null;
            } else {
              _tmpConfirmedTime = _cursor.getLong(_cursorIndexOfConfirmedTime);
            }
            _result = new ReminderLog(_tmpId,_tmpMedicineId,_tmpMedicineName,_tmpScheduledTime,_tmpStatus,_tmpConfirmedTime);
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
  public Object getForMedicineOnDay(final long medicineId, final long startOfDay,
      final long endOfDay, final Continuation<? super ReminderLog> $completion) {
    final String _sql = "SELECT * FROM reminder_logs WHERE medicineId = ? AND scheduledTime >= ? AND scheduledTime < ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 3);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, medicineId);
    _argIndex = 2;
    _statement.bindLong(_argIndex, startOfDay);
    _argIndex = 3;
    _statement.bindLong(_argIndex, endOfDay);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<ReminderLog>() {
      @Override
      @Nullable
      public ReminderLog call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfMedicineId = CursorUtil.getColumnIndexOrThrow(_cursor, "medicineId");
          final int _cursorIndexOfMedicineName = CursorUtil.getColumnIndexOrThrow(_cursor, "medicineName");
          final int _cursorIndexOfScheduledTime = CursorUtil.getColumnIndexOrThrow(_cursor, "scheduledTime");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfConfirmedTime = CursorUtil.getColumnIndexOrThrow(_cursor, "confirmedTime");
          final ReminderLog _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpMedicineId;
            _tmpMedicineId = _cursor.getLong(_cursorIndexOfMedicineId);
            final String _tmpMedicineName;
            _tmpMedicineName = _cursor.getString(_cursorIndexOfMedicineName);
            final long _tmpScheduledTime;
            _tmpScheduledTime = _cursor.getLong(_cursorIndexOfScheduledTime);
            final ReminderStatus _tmpStatus;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfStatus);
            _tmpStatus = __converters.toReminderStatus(_tmp);
            final Long _tmpConfirmedTime;
            if (_cursor.isNull(_cursorIndexOfConfirmedTime)) {
              _tmpConfirmedTime = null;
            } else {
              _tmpConfirmedTime = _cursor.getLong(_cursorIndexOfConfirmedTime);
            }
            _result = new ReminderLog(_tmpId,_tmpMedicineId,_tmpMedicineName,_tmpScheduledTime,_tmpStatus,_tmpConfirmedTime);
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
