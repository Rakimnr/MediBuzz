package com.medibuzz.data;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile MedicineDao _medicineDao;

  private volatile ReminderLogDao _reminderLogDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(1) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `medicines` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `doseNote` TEXT NOT NULL, `hour` INTEGER NOT NULL, `minute` INTEGER NOT NULL, `scheduleType` TEXT NOT NULL, `startDate` INTEGER NOT NULL, `isActive` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `reminder_logs` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `medicineId` INTEGER NOT NULL, `medicineName` TEXT NOT NULL, `scheduledTime` INTEGER NOT NULL, `status` TEXT NOT NULL, `confirmedTime` INTEGER)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '11db75438b377948db14b0253d798280')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `medicines`");
        db.execSQL("DROP TABLE IF EXISTS `reminder_logs`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsMedicines = new HashMap<String, TableInfo.Column>(9);
        _columnsMedicines.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMedicines.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMedicines.put("doseNote", new TableInfo.Column("doseNote", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMedicines.put("hour", new TableInfo.Column("hour", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMedicines.put("minute", new TableInfo.Column("minute", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMedicines.put("scheduleType", new TableInfo.Column("scheduleType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMedicines.put("startDate", new TableInfo.Column("startDate", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMedicines.put("isActive", new TableInfo.Column("isActive", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMedicines.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysMedicines = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesMedicines = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoMedicines = new TableInfo("medicines", _columnsMedicines, _foreignKeysMedicines, _indicesMedicines);
        final TableInfo _existingMedicines = TableInfo.read(db, "medicines");
        if (!_infoMedicines.equals(_existingMedicines)) {
          return new RoomOpenHelper.ValidationResult(false, "medicines(com.medibuzz.data.Medicine).\n"
                  + " Expected:\n" + _infoMedicines + "\n"
                  + " Found:\n" + _existingMedicines);
        }
        final HashMap<String, TableInfo.Column> _columnsReminderLogs = new HashMap<String, TableInfo.Column>(6);
        _columnsReminderLogs.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReminderLogs.put("medicineId", new TableInfo.Column("medicineId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReminderLogs.put("medicineName", new TableInfo.Column("medicineName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReminderLogs.put("scheduledTime", new TableInfo.Column("scheduledTime", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReminderLogs.put("status", new TableInfo.Column("status", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReminderLogs.put("confirmedTime", new TableInfo.Column("confirmedTime", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysReminderLogs = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesReminderLogs = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoReminderLogs = new TableInfo("reminder_logs", _columnsReminderLogs, _foreignKeysReminderLogs, _indicesReminderLogs);
        final TableInfo _existingReminderLogs = TableInfo.read(db, "reminder_logs");
        if (!_infoReminderLogs.equals(_existingReminderLogs)) {
          return new RoomOpenHelper.ValidationResult(false, "reminder_logs(com.medibuzz.data.ReminderLog).\n"
                  + " Expected:\n" + _infoReminderLogs + "\n"
                  + " Found:\n" + _existingReminderLogs);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "11db75438b377948db14b0253d798280", "62a80880e62ff9203adf5b2ac7391ef3");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "medicines","reminder_logs");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `medicines`");
      _db.execSQL("DELETE FROM `reminder_logs`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(MedicineDao.class, MedicineDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(ReminderLogDao.class, ReminderLogDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public MedicineDao medicineDao() {
    if (_medicineDao != null) {
      return _medicineDao;
    } else {
      synchronized(this) {
        if(_medicineDao == null) {
          _medicineDao = new MedicineDao_Impl(this);
        }
        return _medicineDao;
      }
    }
  }

  @Override
  public ReminderLogDao reminderLogDao() {
    if (_reminderLogDao != null) {
      return _reminderLogDao;
    } else {
      synchronized(this) {
        if(_reminderLogDao == null) {
          _reminderLogDao = new ReminderLogDao_Impl(this);
        }
        return _reminderLogDao;
      }
    }
  }
}
