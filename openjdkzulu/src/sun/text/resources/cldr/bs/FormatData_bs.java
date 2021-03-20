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

package sun.text.resources.cldr.bs;

import java.util.ListResourceBundle;

public class FormatData_bs extends ListResourceBundle {
    @Override
    protected final Object[][] getContents() {
        final Object[][] data = new Object[][] {
            { "MonthNames",
                new String[] {
                    "januar",
                    "februar",
                    "mart",
                    "april",
                    "maj",
                    "juni",
                    "juli",
                    "avgust",
                    "septembar",
                    "oktobar",
                    "novembar",
                    "decembar",
                    "",
                }
            },
            { "MonthAbbreviations",
                new String[] {
                    "jan",
                    "feb",
                    "mar",
                    "apr",
                    "maj",
                    "jun",
                    "jul",
                    "avg",
                    "sep",
                    "okt",
                    "nov",
                    "dec",
                    "",
                }
            },
            { "MonthNarrows",
                new String[] {
                    "j",
                    "f",
                    "m",
                    "a",
                    "m",
                    "j",
                    "j",
                    "a",
                    "s",
                    "o",
                    "n",
                    "d",
                    "",
                }
            },
            { "DayNames",
                new String[] {
                    "nedjelja",
                    "ponedjeljak",
                    "utorak",
                    "srijeda",
                    "\u010detvrtak",
                    "petak",
                    "subota",
                }
            },
            { "DayAbbreviations",
                new String[] {
                    "ned",
                    "pon",
                    "uto",
                    "sri",
                    "\u010det",
                    "pet",
                    "sub",
                }
            },
            { "QuarterNames",
                new String[] {
                    "Prvi kvartal",
                    "Drugi kvartal",
                    "Tre\u0107i kvartal",
                    "\u010cetvrti kvartal",
                }
            },
            { "QuarterAbbreviations",
                new String[] {
                    "K1",
                    "K2",
                    "K3",
                    "K4",
                }
            },
            { "AmPmMarkers",
                new String[] {
                    "pre podne",
                    "popodne",
                }
            },
            { "long.Eras",
                new String[] {
                    "Pre nove ere",
                    "Nove ere",
                }
            },
            { "Eras",
                new String[] {
                    "p. n. e.",
                    "n. e",
                }
            },
            { "field.era", "era" },
            { "field.year", "godina" },
            { "field.month", "mesec" },
            { "field.week", "nedelja" },
            { "field.weekday", "dan u nedelji" },
            { "field.dayperiod", "pre podne/ popodne" },
            { "field.hour", "\u010das" },
            { "field.minute", "minut" },
            { "field.second", "sekund" },
            { "field.zone", "zona" },
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
                    "EEEE, dd. MMMM y.",
                    "dd. MMMM y.",
                    "dd.MM.y.",
                    "dd.MM.yy.",
                }
            },
            { "islamic.Eras",
                new String[] {
                    "",
                    "AH",
                }
            },
            { "java.time.islamic.DatePatterns",
                new String[] {
                    "EEEE, dd. MMMM y. G",
                    "dd. MMMM y. G",
                    "dd.MM.y. G",
                    "dd.MM.y. G",
                }
            },
            { "islamic.DatePatterns",
                new String[] {
                    "EEEE, dd. MMMM y. GGGG",
                    "dd. MMMM y. GGGG",
                    "dd.MM.y. GGGG",
                    "dd.MM.y. GGGG",
                }
            },
            { "calendarname.islamic-civil", "Islamski civilni kalendar" },
            { "calendarname.islamicc", "Islamski civilni kalendar" },
            { "calendarname.buddhist", "Budisti\u010dki kalendar" },
            { "calendarname.islamic", "Islamski kalendar" },
            { "calendarname.gregorian", "Gregorijanski kalendar" },
            { "calendarname.gregory", "Gregorijanski kalendar" },
            { "calendarname.roc", "Kalendar Republike Kine" },
            { "calendarname.japanese", "Japanski kalendar" },
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
        };
        return data;
    }
}
