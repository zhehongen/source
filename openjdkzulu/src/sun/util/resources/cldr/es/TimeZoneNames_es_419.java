/*
 * Copyright (c) 2012, 2021, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

/*
 * COPYRIGHT AND PERMISSION NOTICE
 *
 * Copyright (C) 1991-2012 Unicode, Inc. All rights reserved. Distributed under
 * the Terms of Use in http://www.unicode.org/copyright.html.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of the Unicode data files and any associated documentation (the "Data
 * Files") or Unicode software and any associated documentation (the
 * "Software") to deal in the Data Files or Software without restriction,
 * including without limitation the rights to use, copy, modify, merge,
 * publish, distribute, and/or sell copies of the Data Files or Software, and
 * to permit persons to whom the Data Files or Software are furnished to do so,
 * provided that (a) the above copyright notice(s) and this permission notice
 * appear with all copies of the Data Files or Software, (b) both the above
 * copyright notice(s) and this permission notice appear in associated
 * documentation, and (c) there is clear notice in each modified Data File or
 * in the Software as well as in the documentation associated with the Data
 * File(s) or Software that the data or software has been modified.
 *
 * THE DATA FILES AND SOFTWARE ARE PROVIDED "AS IS", WITHOUT WARRANTY OF ANY
 * KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT OF
 * THIRD PARTY RIGHTS. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR HOLDERS
 * INCLUDED IN THIS NOTICE BE LIABLE FOR ANY CLAIM, OR ANY SPECIAL INDIRECT OR
 * CONSEQUENTIAL DAMAGES, OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE,
 * DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER
 * TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE
 * OF THE DATA FILES OR SOFTWARE.
 *
 * Except as contained in this notice, the name of a copyright holder shall not
 * be used in advertising or otherwise to promote the sale, use or other
 * dealings in these Data Files or Software without prior written authorization
 * of the copyright holder.
 */

package sun.util.resources.cldr.es;

import sun.util.resources.TimeZoneNamesBundle;

public class TimeZoneNames_es_419 extends TimeZoneNamesBundle {
    @Override
    protected final Object[][] getContents() {
        final String[] America_Mountain = new String[] {
               "Hora est\u00e1ndar de Monta\u00f1a",
               "MST",
               "Hora de verano de Monta\u00f1a",
               "MDT",
               "Hora de las Monta\u00f1as",
               "MT",
            };
        final String[] America_Pacific = new String[] {
               "Hora est\u00e1ndar del Pac\u00edfico",
               "PST",
               "Hora de verano del Pac\u00edfico",
               "PDT",
               "Hora del Pac\u00edfico",
               "PT",
            };
        final String[] America_Eastern = new String[] {
               "Hora est\u00e1ndar oriental",
               "EST",
               "Hora de verano oriental",
               "EDT",
               "Hora oriental",
               "ET",
            };
        final String[] America_Central = new String[] {
               "Hora est\u00e1ndar central",
               "CST",
               "Hora de verano central",
               "CDT",
               "Hora central",
               "CT",
            };
        final Object[][] data = new Object[][] {
            { "America/Los_Angeles", America_Pacific },
            { "America/Denver", America_Mountain },
            { "America/Phoenix", America_Mountain },
            { "America/Chicago", America_Central },
            { "America/New_York", America_Eastern },
            { "America/Indianapolis", America_Eastern },
            { "America/Inuvik", America_Mountain },
            { "America/Iqaluit", America_Eastern },
            { "America/Matamoros", America_Central },
            { "America/Indiana/Winamac", America_Eastern },
            { "America/El_Salvador", America_Central },
            { "America/Kentucky/Monticello", America_Eastern },
            { "America/Coral_Harbour", America_Eastern },
            { "America/North_Dakota/Center", America_Central },
            { "America/Guatemala", America_Central },
            { "PST8PDT", America_Pacific },
            { "America/Rankin_Inlet", America_Central },
            { "America/Cayman", America_Eastern },
            { "America/Belize", America_Central },
            { "America/Panama", America_Eastern },
            { "CST6CDT", America_Central },
            { "America/Indiana/Tell_City", America_Central },
            { "America/Menominee", America_Central },
            { "America/Tijuana", America_Pacific },
            { "America/Managua", America_Central },
            { "America/Indiana/Petersburg", America_Eastern },
            { "America/Resolute", America_Central },
            { "America/Chihuahua", America_Mountain },
            { "America/Ojinaga", America_Mountain },
            { "America/Merida", America_Central },
            { "America/Mazatlan", America_Mountain },
            { "America/Edmonton", America_Mountain },
            { "America/Tegucigalpa", America_Central },
            { "America/Rainy_River", America_Central },
            { "America/Yellowknife", America_Mountain },
            { "America/Port-au-Prince", America_Eastern },
            { "America/Nipigon", America_Eastern },
            { "America/Indiana/Vevay", America_Eastern },
            { "America/Regina", America_Central },
            { "America/Boise", America_Mountain },
            { "EST5EDT", America_Eastern },
            { "America/North_Dakota/New_Salem", America_Central },
            { "America/Dawson_Creek", America_Mountain },
            { "America/Costa_Rica", America_Central },
            { "America/Dawson", America_Pacific },
            { "America/Shiprock", America_Mountain },
            { "America/Winnipeg", America_Central },
            { "America/Hermosillo", America_Mountain },
            { "America/Indiana/Knox", America_Central },
            { "America/Cancun", America_Central },
            { "America/North_Dakota/Beulah", America_Central },
            { "America/Thunder_Bay", America_Eastern },
            { "America/Swift_Current", America_Central },
            { "America/Grand_Turk", America_Eastern },
            { "America/Metlakatla", America_Pacific },
            { "America/Bahia_Banderas", America_Central },
            { "America/Pangnirtung", America_Eastern },
            { "America/Santa_Isabel", America_Pacific },
            { "America/Cambridge_Bay", America_Mountain },
            { "America/Toronto", America_Eastern },
            { "America/Indiana/Marengo", America_Eastern },
            { "MST7MDT", America_Mountain },
            { "America/Creston", America_Mountain },
            { "America/Monterrey", America_Central },
            { "America/Indiana/Vincennes", America_Eastern },
            { "America/Whitehorse", America_Pacific },
            { "America/Nassau", America_Eastern },
            { "America/Mexico_City", America_Central },
            { "America/Jamaica", America_Eastern },
            { "America/Louisville", America_Eastern },
            { "America/Vancouver", America_Pacific },
            { "America/Montreal", America_Eastern },
            { "America/Detroit", America_Eastern },
        };
        return data;
    }
}
