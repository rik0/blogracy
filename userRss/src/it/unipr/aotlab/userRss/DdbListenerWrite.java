/*
 * Copyright (c)  2011 Alan Nonnato, Enrico Franchi, Michele Tomaiuolo and University of Parma.
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

package it.unipr.aotlab.userRss;


import org.gudy.azureus2.plugins.ddb.DistributedDatabaseEvent;
import org.gudy.azureus2.plugins.ddb.DistributedDatabaseListener;

public class DdbListenerWrite implements DistributedDatabaseListener {


    @Override
    public void event(DistributedDatabaseEvent event) {
        //get the event type
        int type = event.getType();
        //if the plugin start to write
        if (type == DistributedDatabaseEvent.ET_OPERATION_STARTS) //i start to write
        {
            System.out.println("i'm starting write on DDB");
        } else if (type == DistributedDatabaseEvent.ET_OPERATION_TIMEOUT) {//timeout Error
            System.out.println("Timeout error!\n 1000 seconds elapsed without any response from server.");
        } else if (event.getType() == DistributedDatabaseEvent.ET_OPERATION_COMPLETE) {//write successful
            System.out.println("Data correctly write on DDB");
        }
    }
}