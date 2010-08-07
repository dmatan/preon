/**
 * Copyright (C) 2009-2010 Wilfred Springer
 *
 * This file is part of Preon.
 *
 * Preon is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2, or (at your option) any later version.
 *
 * Preon is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Preon; see the file COPYING. If not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Linking this library statically or dynamically with other modules is making a
 * combined work based on this library. Thus, the terms and conditions of the
 * GNU General Public License cover the whole combination.
 *
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules to produce an
 * executable, regardless of the license terms of these independent modules, and
 * to copy and distribute the resulting executable under terms of your choice,
 * provided that you also meet, for each linked independent module, the terms
 * and conditions of the license of that module. An independent module is a
 * module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but
 * you are not obligated to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
package org.codehaus.preon.codec;


import nl.flotsam.limbo.Expression;
import org.codehaus.preon.*;
import org.codehaus.preon.buffer.BitBuffer;
import org.codehaus.preon.channel.BitChannel;
import org.codehaus.preon.descriptor.PassThroughCodecDescriptor2;

import java.lang.reflect.AnnotatedElement;

/**
 * A {@link CodecDecorator} that will log a message before and after the invocation of every {@link Codec}.
 *
 * @author Wilfred Springer (wis)
 */
public class LoggingDecorator implements CodecDecorator {

    /** The actual logging object. */
    private Logger logger;

    /**
     * Constructs a new instance, accepting the {@link Logger} to use.
     *
     * @param logger The {@link Logger} to use.
     */
    public LoggingDecorator(Logger logger) {
        this.logger = logger;
    }

    /** Constructs a new instance that will log to System.out. */
    public LoggingDecorator() {
        this(new DefaultLogger());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.codehaus.preon.CodecDecorator#decorate(org.codehaus.preon.Codec,
     * java.lang.reflect.AnnotatedElement, java.lang.Class,
     * org.codehaus.preon.ResolverContext)
     */

    public <T> Codec<T> decorate(Codec<T> decorated, AnnotatedElement metadata,
                                 Class<T> type, ResolverContext context) {
        return new LoggingCodec<T>(decorated, logger);
    }

    /** The object that will generate the messages. Receives events for anything of interest. */
    public interface Logger {

        /**
         * The operation called whenever a {@link Codec} kicks in.
         *
         * @param codec    The {@link Codec} called.
         * @param position The position in the {@link BitBuffer}.
         * @param size     The expected number of bits that will be read, or <code>-1</code> if unknown in advance.
         */
        void logStartDecoding(Codec<?> codec, long position, long size);

        /**
         * The operation called whenever a {@link Codec} is done.
         *
         * @param codec    The {@link Codec} called.
         * @param position The position in the {@link BitBuffer}.
         * @param read     The number of bits that actually have been read.
         * @param result   The value decoded by the {@link Codec}.
         */
        void logDoneDecoding(Codec<?> codec, long position, long read,
                             Object result);

        /** The operation called when the {@link Codec} failed to decode a value. */
        void logFailedDecoding();

    }

    /**
     * A default implementation of the {@link Logger} interface, logging to System.out.
     *
     * @author Wilfred Springer (wis)
     */
    private static class DefaultLogger implements Logger {

        /** "Call-stack depth" */
        private int level = 0;

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.codehaus.preon.codec.LoggingDecorator.Logger#logDoneDecoding(nl
         * .flotsam.preon.Codec, long, long, java.lang.Object)
         */

        public void logDoneDecoding(Codec<?> codec, long position, long read,
                                    Object result) {
            level--;
            // TODO: 
            printMessage("Done decoding at " + position
                    + " (" + read + " bits) : " + format(result) + " Codec: " + codec);
            System.out.println();
        }

        /**
         * Formats the result.
         *
         * @param result The object.
         * @return The result formatted as a String.
         */
        private String format(Object result) {
            if (result instanceof Integer) {
                return result.toString() + " (0x"
                        + Integer.toHexString((Integer) result) + ")";
            } else if (result instanceof Long) {
                return result.toString() + " (0x"
                        + Long.toHexString((Long) result) + ")";
            } else if (result != null) {
                return result.toString();
            } else {
                return "null";
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.codehaus.preon.codec.LoggingDecorator.Logger#logFailedDecoding()
         */

        public void logFailedDecoding() {
            level--;
            printMessage("Failed decoding.");
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.codehaus.preon.codec.LoggingDecorator.Logger#logStartDecoding(nl
         * .flotsam.preon.Codec, long, long)
         */

        public void logStartDecoding(Codec<?> codec, long position, long size) {
            StringBuilder builder = new StringBuilder();
            builder.append("Start decoding at ")
                    .append(position)
                    .append(size >= 0 ? " (maximal up to " + (position + size) + ")" : "")
                    .append(" Codec: ")
                    .append(codec.toString());
            printMessage(builder.toString());
            level++;
        }

        /**
         * Prints the message. (Used both by {@link #logDoneDecoding(Codec, long, long, Object)}, {@link
         * #logStartDecoding(Codec, long, long)}, as well as {@link #logFailedDecoding()}.
         *
         * @param message The message to be printed.
         */
        private void printMessage(String message) {
            for (int i = 0; i < level; i++) {
                System.out.print(' ');
            }
            System.out.println(message);
        }

    }

    /**
     * The {@link Codec} constructed by the {@link LoggingDecorator}.
     *
     * @author Wilfred Springer (wis)
     * @param <T>
     */
    private static class LoggingCodec<T> implements Codec<T> {

        /** The {@link Codec} wrapped. */
        private final Codec<T> codec;

        /** The {@link Logger} to use. */
        private final Logger logger;

        /**
         * Constructs a new instance.
         *
         * @param codec  The {@link Codec} to wrap.
         * @param logger The {@link Logger} to use.
         */
        public LoggingCodec(Codec<T> codec, Logger logger) {
            assert codec != null;
            assert logger != null;
            this.codec = codec;
            this.logger = logger;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.codehaus.preon.Codec#decode(org.codehaus.preon.buffer.BitBuffer,
         * org.codehaus.preon.Resolver, org.codehaus.preon.Builder)
         */

        public T decode(BitBuffer buffer, Resolver resolver, Builder builder)
                throws DecodingException {
            T result = null;
            long pos = buffer.getActualBitPos();
            logger.logStartDecoding(codec, pos, LoggingDecorator.getSize(codec, resolver));
            try {
                result = codec.decode(buffer, resolver, builder);
            } catch (DecodingException de) {
                logger.logFailedDecoding();
                throw de;
            } finally {
                logger.logDoneDecoding(codec, buffer.getActualBitPos(), buffer
                        .getActualBitPos()
                        - pos, result);
            }
            return result;
        }

        public void encode(T object, BitChannel channel, Resolver resolver) {
            throw new UnsupportedOperationException();
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.codehaus.preon.Codec#getTypes()
         */

        public Class<?>[] getTypes() {
            return codec.getTypes();
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.codehaus.preon.Codec#getSize()
         */

        public Expression<Integer, Resolver> getSize() {
            return codec.getSize();
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.codehaus.preon.Codec#getType()
         */

        public Class<?> getType() {
            return codec.getType();
        }

        public CodecDescriptor getCodecDescriptor() {
            return new PassThroughCodecDescriptor2(codec.getCodecDescriptor(),
                    false);
        }

    }

    private final static long getSize(Codec<?> codec, Resolver resolver) {
        Expression<Integer, Resolver> size = codec.getSize();
        if (size == null) {
            return -1;
        } else {
            if (size.isParameterized()) {
                return -1;
            } else {
                return size.eval(resolver);
            }
        }
    }

}