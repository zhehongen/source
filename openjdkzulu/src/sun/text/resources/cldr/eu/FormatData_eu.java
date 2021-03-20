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

package sun.text.resources.cldr.eu;

import java.util.ListResourceBundle;

public class FormatData_eu extends ListResourceBundle {
    @Override
    protected final Object[][] getContents() {
        final Object[][] data = new Object[][] {
            { "MonthNames",
                new String[] {
                    "urtarrila",
                    "otsaila",
                    "martxoa",
                    "apirila",
                    "maiatza",
                    "ekaina",
                    "uztaila",
                    "abuztua",
                    "iraila",
                    "urria",
                    "azaroa",
                    "abendua",
                    "",
                }
            },
            { "MonthAbbreviations",
                new String[] {
                    "urt",
                    "ots",
                    "mar",
                    "api",
                    "mai",
                    "eka",
                    "uzt",
                    "abu",
                    "ira",
                    "urr",
                    "aza",
                    "abe",
                    "",
                }
            },
            { "MonthNarrows",
                new String[] {
                    "U",
                    "O",
                    "M",
                    "A",
                    "M",
                    "E",
                    "U",
                    "A",
                    "I",
                    "U",
                    "A",
                    "A",
                    "",
                }
            },
            { "DayNames",
                new String[] {
                    "igandea",
                    "astelehena",
                    "asteartea",
                    "asteazkena",
                    "osteguna",
                    "ostirala",
                    "larunbata",
                }
            },
            { "DayAbbreviations",
                new String[] {
                    "ig",
                    "al",
                    "as",
                    "az",
                    "og",
                    "or",
                    "lr",
                }
            },
            { "DayNarrows",
                new String[] {
                    "I",
                    "M",
                    "A",
                    "A",
                    "A",
                    "O",
                    "I",
                }
            },
            { "standalone.DayNarrows",
                new String[] {
                    "I",
                    "M",
                    "A",
                    "L",
                    "A",
                    "O",
                    "I",
                }
            },
            { "QuarterNames",
                new String[] {
                    "1. hiruhilekoa",
                    "2. hiruhilekoa",
                    "3. hiruhilekoa",
                    "4. hiruhilekoa",
                }
            },
            { "QuarterAbbreviations",
                new String[] {
                    "1Hh",
                    "2Hh",
                    "3Hh",
                    "4Hh",
                }
            },
            { "QuarterNarrows",
                new String[] {
                    "1",
                    "2",
                    "3",
                    "4",
                }
            },
            { "narrow.AmPmMarkers",
                new String[] {
                    "a",
                    "p",
                }
            },
            { "Eras",
                new String[] {
                    "K.a.",
                    "K.o.",
                }
            },
            { "field.era", "Aroa" },
            { "field.year", "Urtea" },
            { "field.month", "Hilabetea" },
            { "field.week", "Astea" },
            { "field.weekday", "Asteguna" },
            { "field.dayperiod", "AM//PM" },
            { "field.hour", "Ordua" },
            { "field.minute", "Minutuak" },
            { "field.second", "Segundoak" },
            { "field.zone", "Ordu-eremua" },
            { "TimePatterns",
                new String[] {
                    "HH:mm:ss zzzz",
                    "HH:mm:ss z",
                    "HH:mm:ss",
                    "HH:mm",
                }
            },
            { "DatePatterns",
                new String[] {
                    "EEEE, y'eko' MMMM'ren' dd'a'",
                    "y'eko' MMM'ren' dd'a'",
                    "y MMM d",
                    "yyyy-MM-dd",
                }
            },
            { "buddhist.Eras",
                new String[] {
                    "BC",
                    "BG",
                }
            },
            { "roc.Eras",
                new String[] {
                    "R.O.C. aurretik",
                    "R.O.C.",
                }
            },
            { "calendarname.islamic-civil", "Islamiar egutegi zibila" },
            { "calendarname.islamicc", "Islamiar egutegi zibila" },
            { "calendarname.gregorian", "Egutegi gregoriarra" },
            { "calendarname.gregory", "Egutegi gregoriarra" },
            { "calendarname.japanese", "Japoniar egutegia" },
            { "calendarname.buddhist", "Egutegi budista" },
            { "calendarname.islamic", "Islamiar egutegia" },
            { "calendarname.roc", "Txinako Errepublikako egutegia" },
            { "DefaultNumberingSystem", "latn" },
            { "latn.NumberElements",
                new String[] {
                    ",",
                    ".",
                    ";",
                    "%",
                    "0",
                    "#",
                    "-",
                    "E",
                    "\u2030",
                    "\u221e",
                    "NaN",
                }
            },
            { "NumberPatterns",
                new String[] {
                    "#,##0.###",
                    "#,##0.00\u00a0\u00a4",
                    "#,##0%",
                }
            },
        };
        return data;
    }
}
