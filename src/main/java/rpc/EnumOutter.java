// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: enum.proto

package rpc;

public final class EnumOutter {
  private EnumOutter() {}
  public static void registerAllExtensions(
          com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
          com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
            (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  /**
   * Protobuf enum {@code protobuf.EntryType}
   */
  public enum EntryType
          implements com.google.protobuf.ProtocolMessageEnum {
    /**
     * <code>ENTRY_TYPE_UNKNOWN = 0;</code>
     */
    ENTRY_TYPE_UNKNOWN(0),
    /**
     * <code>ENTRY_TYPE_NO_OP = 1;</code>
     */
    ENTRY_TYPE_NO_OP(1),
    /**
     * <code>ENTRY_TYPE_DATA = 2;</code>
     */
    ENTRY_TYPE_DATA(2),
    /**
     * <code>ENTRY_TYPE_CONFIGURATION = 3;</code>
     */
    ENTRY_TYPE_CONFIGURATION(3),
    UNRECOGNIZED(-1),
    ;

    /**
     * <code>ENTRY_TYPE_UNKNOWN = 0;</code>
     */
    public static final int ENTRY_TYPE_UNKNOWN_VALUE = 0;
    /**
     * <code>ENTRY_TYPE_NO_OP = 1;</code>
     */
    public static final int ENTRY_TYPE_NO_OP_VALUE = 1;
    /**
     * <code>ENTRY_TYPE_DATA = 2;</code>
     */
    public static final int ENTRY_TYPE_DATA_VALUE = 2;
    /**
     * <code>ENTRY_TYPE_CONFIGURATION = 3;</code>
     */
    public static final int ENTRY_TYPE_CONFIGURATION_VALUE = 3;


    public final int getNumber() {
      if (this == UNRECOGNIZED) {
        throw new java.lang.IllegalArgumentException(
                "Can't get the number of an unknown enum value.");
      }
      return value;
    }

    /**
     * @deprecated Use {@link #forNumber(int)} instead.
     */
    @java.lang.Deprecated
    public static EntryType valueOf(int value) {
      return forNumber(value);
    }

    public static EntryType forNumber(int value) {
      switch (value) {
        case 0: return ENTRY_TYPE_UNKNOWN;
        case 1: return ENTRY_TYPE_NO_OP;
        case 2: return ENTRY_TYPE_DATA;
        case 3: return ENTRY_TYPE_CONFIGURATION;
        default: return null;
      }
    }

    public static com.google.protobuf.Internal.EnumLiteMap<EntryType>
    internalGetValueMap() {
      return internalValueMap;
    }
    private static final com.google.protobuf.Internal.EnumLiteMap<
            EntryType> internalValueMap =
            new com.google.protobuf.Internal.EnumLiteMap<EntryType>() {
              public EntryType findValueByNumber(int number) {
                return EntryType.forNumber(number);
              }
            };

    public final com.google.protobuf.Descriptors.EnumValueDescriptor
    getValueDescriptor() {
      return getDescriptor().getValues().get(ordinal());
    }
    public final com.google.protobuf.Descriptors.EnumDescriptor
    getDescriptorForType() {
      return getDescriptor();
    }
    public static final com.google.protobuf.Descriptors.EnumDescriptor
    getDescriptor() {
      return rpc.EnumOutter.getDescriptor().getEnumTypes().get(0);
    }

    private static final EntryType[] VALUES = values();

    public static EntryType valueOf(
            com.google.protobuf.Descriptors.EnumValueDescriptor desc) {
      if (desc.getType() != getDescriptor()) {
        throw new java.lang.IllegalArgumentException(
                "EnumValueDescriptor is not for this type.");
      }
      if (desc.getIndex() == -1) {
        return UNRECOGNIZED;
      }
      return VALUES[desc.getIndex()];
    }

    private final int value;

    private EntryType(int value) {
      this.value = value;
    }

    // @@protoc_insertion_point(enum_scope:protobuf.EntryType)
  }

  /**
   * Protobuf enum {@code protobuf.ErrorType}
   */
  public enum ErrorType
          implements com.google.protobuf.ProtocolMessageEnum {
    /**
     * <code>ERROR_TYPE_NONE = 0;</code>
     */
    ERROR_TYPE_NONE(0),
    /**
     * <code>ERROR_TYPE_LOG = 1;</code>
     */
    ERROR_TYPE_LOG(1),
    /**
     * <code>ERROR_TYPE_STABLE = 2;</code>
     */
    ERROR_TYPE_STABLE(2),
    /**
     * <code>ERROR_TYPE_SNAPSHOT = 3;</code>
     */
    ERROR_TYPE_SNAPSHOT(3),
    /**
     * <code>ERROR_TYPE_STATE_MACHINE = 4;</code>
     */
    ERROR_TYPE_STATE_MACHINE(4),
    /**
     * <code>ERROR_TYPE_META = 5;</code>
     */
    ERROR_TYPE_META(5),
    UNRECOGNIZED(-1),
    ;

    /**
     * <code>ERROR_TYPE_NONE = 0;</code>
     */
    public static final int ERROR_TYPE_NONE_VALUE = 0;
    /**
     * <code>ERROR_TYPE_LOG = 1;</code>
     */
    public static final int ERROR_TYPE_LOG_VALUE = 1;
    /**
     * <code>ERROR_TYPE_STABLE = 2;</code>
     */
    public static final int ERROR_TYPE_STABLE_VALUE = 2;
    /**
     * <code>ERROR_TYPE_SNAPSHOT = 3;</code>
     */
    public static final int ERROR_TYPE_SNAPSHOT_VALUE = 3;
    /**
     * <code>ERROR_TYPE_STATE_MACHINE = 4;</code>
     */
    public static final int ERROR_TYPE_STATE_MACHINE_VALUE = 4;
    /**
     * <code>ERROR_TYPE_META = 5;</code>
     */
    public static final int ERROR_TYPE_META_VALUE = 5;


    public final int getNumber() {
      if (this == UNRECOGNIZED) {
        throw new java.lang.IllegalArgumentException(
                "Can't get the number of an unknown enum value.");
      }
      return value;
    }

    /**
     * @deprecated Use {@link #forNumber(int)} instead.
     */
    @java.lang.Deprecated
    public static ErrorType valueOf(int value) {
      return forNumber(value);
    }

    public static ErrorType forNumber(int value) {
      switch (value) {
        case 0: return ERROR_TYPE_NONE;
        case 1: return ERROR_TYPE_LOG;
        case 2: return ERROR_TYPE_STABLE;
        case 3: return ERROR_TYPE_SNAPSHOT;
        case 4: return ERROR_TYPE_STATE_MACHINE;
        case 5: return ERROR_TYPE_META;
        default: return null;
      }
    }

    public static com.google.protobuf.Internal.EnumLiteMap<ErrorType>
    internalGetValueMap() {
      return internalValueMap;
    }
    private static final com.google.protobuf.Internal.EnumLiteMap<
            ErrorType> internalValueMap =
            new com.google.protobuf.Internal.EnumLiteMap<ErrorType>() {
              public ErrorType findValueByNumber(int number) {
                return ErrorType.forNumber(number);
              }
            };

    public final com.google.protobuf.Descriptors.EnumValueDescriptor
    getValueDescriptor() {
      return getDescriptor().getValues().get(ordinal());
    }
    public final com.google.protobuf.Descriptors.EnumDescriptor
    getDescriptorForType() {
      return getDescriptor();
    }
    public static final com.google.protobuf.Descriptors.EnumDescriptor
    getDescriptor() {
      return rpc.EnumOutter.getDescriptor().getEnumTypes().get(1);
    }

    private static final ErrorType[] VALUES = values();

    public static ErrorType valueOf(
            com.google.protobuf.Descriptors.EnumValueDescriptor desc) {
      if (desc.getType() != getDescriptor()) {
        throw new java.lang.IllegalArgumentException(
                "EnumValueDescriptor is not for this type.");
      }
      if (desc.getIndex() == -1) {
        return UNRECOGNIZED;
      }
      return VALUES[desc.getIndex()];
    }

    private final int value;

    private ErrorType(int value) {
      this.value = value;
    }

    // @@protoc_insertion_point(enum_scope:protobuf.ErrorType)
  }


  public static com.google.protobuf.Descriptors.FileDescriptor
  getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
          descriptor;
  static {
    java.lang.String[] descriptorData = {
            "\n\nenum.proto\022\010protobuf*l\n\tEntryType\022\026\n\022E" +
                    "NTRY_TYPE_UNKNOWN\020\000\022\024\n\020ENTRY_TYPE_NO_OP\020" +
                    "\001\022\023\n\017ENTRY_TYPE_DATA\020\002\022\034\n\030ENTRY_TYPE_CON" +
                    "FIGURATION\020\003*\227\001\n\tErrorType\022\023\n\017ERROR_TYPE" +
                    "_NONE\020\000\022\022\n\016ERROR_TYPE_LOG\020\001\022\025\n\021ERROR_TYP" +
                    "E_STABLE\020\002\022\027\n\023ERROR_TYPE_SNAPSHOT\020\003\022\034\n\030E" +
                    "RROR_TYPE_STATE_MACHINE\020\004\022\023\n\017ERROR_TYPE_" +
                    "META\020\005B\021\n\003rpcB\nEnumOutterb\006proto3"
    };
    com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
            new com.google.protobuf.Descriptors.FileDescriptor.    InternalDescriptorAssigner() {
              public com.google.protobuf.ExtensionRegistry assignDescriptors(
                      com.google.protobuf.Descriptors.FileDescriptor root) {
                descriptor = root;
                return null;
              }
            };
    com.google.protobuf.Descriptors.FileDescriptor
            .internalBuildGeneratedFileFrom(descriptorData,
                    new com.google.protobuf.Descriptors.FileDescriptor[] {
                    }, assigner);
  }

  // @@protoc_insertion_point(outer_class_scope)
}
