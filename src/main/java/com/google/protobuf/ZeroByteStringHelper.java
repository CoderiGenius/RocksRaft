/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.protobuf;

import com.alipay.remoting.util.StringUtils;
import core.NodeImpl;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Byte string and byte buffer converter
 *
 * @author boyan (boyan@alibaba-inc.com)
 *
 * 2018-May-08 2:38:42 PM
 */
public class ZeroByteStringHelper {
    public static final Logger LOG = LoggerFactory.getLogger(ZeroByteStringHelper.class);
    /**
     * Wrap a byte array into a ByteString.
     */
    public static ByteString wrap(final byte[] bs) {
        return ByteString.wrap(bs);
    }

    /**
     * Wrap a byte array into a ByteString.
     * @param bs     the byte array
     * @param offset read start offset in array
     * @param len    read data length
     *@return the    result byte string.
     */
    public static ByteString wrap(final byte[] bs, final int offset, final int len) {
        return ByteString.wrap(bs, offset, len);
    }

    public static byte[] getByteArrayFromByteBuffer(ByteBuffer byteBuffer) {
        byte[] bytesArray = new byte[byteBuffer.remaining()];
        byteBuffer.get(bytesArray, 0, bytesArray.length);
        return bytesArray;
    }


    /**
     * Wrap a byte buffer into a ByteString.
     */
    public static ByteString wrap(final ByteBuffer buf) {
        return ByteString.wrap(buf);
    }

    /**
     * Carry the byte[] from {@link ByteString}, if failed,
     * then call {@link ByteString#toByteArray()}.
     *
     * @param byteString the byteString source data
     * @return carried bytes
     */
    public static byte[] getByteArray(final ByteString byteString) {
        final BytesCarrier carrier = new BytesCarrier();
        try {
            byteString.writeTo(carrier);
            if (carrier.isValid()) {
                return carrier.getValue();
            }else {
                LOG.warn("getByteArray invalid");
            }
        } catch (final IOException ignored) {
            // ignored
            LOG.error("getByteArray IO Exception:{}",ignored.getMessage());
        }
        return byteString.toByteArray();
    }

    public static String byteStringToString(ByteString src) {
        String charSet = "utf-8";
        if(StringUtils.isEmpty(charSet)) {
            charSet = "GB2312";
        }
        return bytesToString(src.toByteArray(), charSet);
    }

    public static String byteBufferToString(ByteBuffer byteBuffer) {
       return bytesToString(getByteArrayFromByteBuffer(byteBuffer),"utf-8");
    }

    public static String bytesToString(byte[] input, String charSet) {
        if(ArrayUtils.isEmpty(input)) {
            return StringUtils.EMPTY;
        }

        ByteBuffer buffer = ByteBuffer.allocate(input.length);
        buffer.put(input);
        buffer.flip();

        Charset charset = null;
        CharsetDecoder decoder = null;
        CharBuffer charBuffer = null;

        try {
            charset = Charset.forName(charSet);
            decoder = charset.newDecoder();
            charBuffer = decoder.decode(buffer.asReadOnlyBuffer());

            return charBuffer.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }




    /**
     * Concatenate the given strings while performing various optimizations to
     * slow the growth rate of tree depth and tree node count. The result is
     * either a {@link com.google.protobuf.ByteString.LeafByteString} or a
     * {@link RopeByteString} depending on which optimizations, if any, were
     * applied.
     *
     * <p>Small pieces of length less than {@link
     * ByteString#CONCATENATE_BY_COPY_SIZE} may be copied by value here, as in
     * BAP95.  Large pieces are referenced without copy.
     *
     * <p>Most of the operation here is inspired by the now-famous paper <a
     *  * href="https://web.archive.org/web/20060202015456/http://www.cs.ubc.ca/local/reading/proceedings/spe91-95/spe/vol25/issue12/spe986.pdf">
     *
     * @param left  string on the left
     * @param right string on the right
     * @return concatenation representing the same sequence as the given strings
     */
    public static ByteString concatenate(final ByteString left, final ByteString right) {
        return RopeByteString.concatenate(left, right);
    }

    /**
     * @see #concatenate(ByteString, ByteString)
     */
    public static ByteString concatenate(final List<ByteBuffer> byteBuffers) {
        final int size = byteBuffers.size();
        if (size == 0) {
            return null;
        }
        if (size == 1) {
            return wrap(byteBuffers.get(0));
        }

        ByteString left = null;
        for (final ByteBuffer buf : byteBuffers) {
            if (buf.remaining() > 0) {
                if (left == null) {
                    left = wrap(buf);
                } else {
                    left = concatenate(left, wrap(buf));
                }
            }
        }
        return left;
    }
}
