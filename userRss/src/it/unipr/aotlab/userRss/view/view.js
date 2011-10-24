/*
 * Copyright (c)  2011 Enrico Franchi and University of Parma.
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

var filter = null;

var updateFollowees = function() {
    return [];
}

var updateFollowers = function() {
    return [];
}

var updateMessages = function(filter) {
    if (filter === null) {
        return [];
    } else {
        return [];
    }
}

var appendFollower = function(follower) {

}

var appendFollowee = function(follower) {

}

var appendMessage = function(follower) {

}

var extractTags = function(messages) {
    return []
}

var update = function() {
    var tags;
    var followees = updateFollowees();
    var followers = updateFollowers();
    var messages = updateMessages(filter);

    for (var follower in followers) {
        appendFollower(follower);
    }
    for (var followee in followees) {
        appendFollowee(followee);
    }
    for (var message in messages) {
        appendMessage(message);
    }
    tags = extractTags(messages);
}