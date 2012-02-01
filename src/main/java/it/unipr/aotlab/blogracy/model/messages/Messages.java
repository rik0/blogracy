/*
 * Copyright (c)  2011 Enrico Franchi, Michele Tomaiuolo and University of Parma.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package it.unipr.aotlab.blogracy.model.messages;

import it.unipr.aotlab.blogracy.model.hashes.Hash;
import it.unipr.aotlab.blogracy.model.users.User;
import it.unipr.aotlab.blogracy.network.Network;
import it.unipr.aotlab.blogracy.network.NetworkManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * User: enrico
 * Package: it.unipr.aotlab.blogracy.model.messages
 * Date: 10/27/11
 * Time: 1:29 PM
 */
public class Messages {
    static class OrderedElement<E> implements Comparable<OrderedElement<E>>{
        final E value;
        final Integer index;

        OrderedElement(final E value, final int index) {
            this.value = value;
            this.index = index;
        }

        @Override
        public int compareTo(final OrderedElement<E> eOrderedElement) {
            return index.compareTo(eOrderedElement.index);
        }

        public E getValue() {
            return value;
        }
    }

    private static List<OrderedElement<InputMessageFilter>> inputMessageFilters =
            new ArrayList<OrderedElement<InputMessageFilter>> ();
    private static List<OrderedElement<OutputMessageFilter>> outputMessageFilters =
            new ArrayList<OrderedElement<OutputMessageFilter>>();
    
    public static Message newMessage(String title, Date date, String text, User author) {
        /* We have not defined a message implementation yet */

        throw new UnsupportedOperationException();
    }
    
    public static Message getMessage(Hash messageHash) throws Exception {
        Network network = NetworkManager.getNetwork();
        Message message = null; // get message from network here (finish network);
        message = visitInputMessageChain(message);
        return message;
    }

    private static Message visitInputMessageChain(Message message) throws Exception {
        for(OrderedElement<InputMessageFilter> orderedInputFilter: inputMessageFilters) {
            InputMessageFilter inputFilter = orderedInputFilter.getValue();
            message = inputFilter.process(message);
        }
        return message;
    }

    public static void postMessage(Message message) throws Exception {
        message = visitOutputMessageChain(message);
        throw new UnsupportedOperationException();
    }

    private static Message visitOutputMessageChain(Message message) throws Exception {
        for(OrderedElement<OutputMessageFilter> orderedOutputFilter: outputMessageFilters) {
            OutputMessageFilter outputMessageFilter = orderedOutputFilter.getValue();
            message = outputMessageFilter.process(message);
        }
        return message;
    }

    public static void addInputMessageFilter(InputMessageFilter messageFilter, int priority) {
        final OrderedElement<InputMessageFilter> newElement 
                = new OrderedElement<InputMessageFilter>(messageFilter, priority);
        inputMessageFilters.add(newElement);
        Collections.sort(inputMessageFilters);
    }

    public static void addOutputMessageFilter(OutputMessageFilter messageFilter, int priority) {
        final OrderedElement<OutputMessageFilter> newElement
                = new OrderedElement<OutputMessageFilter>(messageFilter, priority);
        outputMessageFilters.add(newElement);
        Collections.sort(outputMessageFilters);
    }
}
