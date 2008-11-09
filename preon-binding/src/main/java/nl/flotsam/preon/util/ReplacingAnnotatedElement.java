/*
 * Copyright (C) 2008 Wilfred Springer
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
 * Preon; see the file COPYING. If not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
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

package nl.flotsam.preon.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;

/**
 * An {@link AnnotatedElement} wrapper, replacing one of the annotations with
 * another one.
 * 
 * @author Wilfred Springer (wis)
 * 
 */
public class ReplacingAnnotatedElement implements AnnotatedElement {

    /**
     * The replacement annotation.
     */
    private Annotation replacement;

    /**
     * The other annotations.
     */
    private AnnotatedElement wrapped;

    public ReplacingAnnotatedElement(AnnotatedElement wrapped, Annotation replacement) {
        this.wrapped = wrapped;
        this.replacement = replacement;
    }
    
    /*
     * (non-Javadoc)
     * @see java.lang.reflect.AnnotatedElement#getAnnotation(java.lang.Class)
     */
    public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
        if (annotationType.isAssignableFrom(replacement.getClass())) {
            return (T) replacement;
        } else {
            return wrapped.getAnnotation(annotationType);
        }
    }

    /*
     * (non-Javadoc)
     * @see java.lang.reflect.AnnotatedElement#getAnnotations()
     */
    public Annotation[] getAnnotations() {
        Annotation[] annotations = wrapped.getAnnotations();
        for (int i = 0; i < annotations.length; i++) {
            if (annotations[i].getClass().isAssignableFrom(replacement.getClass())) {
                annotations[i] = replacement;
            }
        }
        return annotations;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.reflect.AnnotatedElement#getDeclaredAnnotations()
     */
    public Annotation[] getDeclaredAnnotations() {
        Annotation[] annotations = wrapped.getDeclaredAnnotations();
        for (int i = 0; i < annotations.length; i++) {
            if (annotations[i].getClass() == replacement.getClass()) {
                annotations[i] = replacement;
            }
        }
        return annotations;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.reflect.AnnotatedElement#isAnnotationPresent(java.lang.Class)
     */
    public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
        if (wrapped.isAnnotationPresent(annotationType)) {
            return true;
        } else {
            return replacement.getClass() == annotationType;
        }
    }

}