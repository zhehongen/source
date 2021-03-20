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

package sun.text.resources.cldr.pt;

import java.util.ListResourceBundle;

public class FormatData_pt extends ListResourceBundle {
    @Override
    protected final Object[][] getContents() {
        final Object[][] data = new Object[][] {
            { "MonthNames",
                new String[] {
                    "janeiro",
                    "fevereiro",
                    "mar\u00e7o",
                    "abril",
                    "maio",
                    "junho",
                    "julho",
                    "agosto",
                    "setembro",
                    "outubro",
                    "novembro",
                    "dezembro",
                    "",
                }
            },
            { "MonthAbbreviations",
                new String[] {
                    "jan",
                    "fev",
                    "mar",
                    "abr",
                    "mai",
                    "jun",
                    "jul",
                    "ago",
                    "set",
                    "out",
                    "nov",
                    "dez",
                    "",
                }
            },
            { "MonthNarrows",
                new String[] {
                    "J",
                    "F",
                    "M",
                    "A",
                    "M",
                    "J",
                    "J",
                    "A",
                    "S",
                    "O",
                    "N",
                    "D",
                    "",
                }
            },
            { "DayNames",
                new String[] {
                    "domingo",
                    "segunda-feira",
                    "ter\u00e7a-feira",
                    "quarta-feira",
                    "quinta-feira",
                    "sexta-feira",
                    "s\u00e1bado",
                }
            },
            { "DayAbbreviations",
                new String[] {
                    "dom",
                    "seg",
                    "ter",
                    "qua",
                    "qui",
                    "sex",
                    "s\u00e1b",
                }
            },
            { "DayNarrows",
                new String[] {
                    "D",
                    "S",
                    "T",
                    "Q",
                    "Q",
                    "S",
                    "S",
                }
            },
            { "QuarterNames",
                new String[] {
                    "1\u00ba trimestre",
                    "2\u00ba trimestre",
                    "3\u00ba trimestre",
                    "4\u00ba trimestre",
                }
            },
            { "standalone.QuarterNames",
                new String[] {
                    "1\u00ba trimestre",
                    "2\u00ba trimestre",
                    "3\u00ba trimestre",
                    "4\u00ba trimestre",
                }
            },
            { "QuarterAbbreviations",
                new String[] {
                    "T1",
                    "T2",
                    "T3",
                    "T4",
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
            { "long.Eras",
                new String[] {
                    "Antes de Cristo",
                    "Ano do Senhor",
                }
            },
            { "Eras",
                new String[] {
                    "a.C.",
                    "d.C.",
                }
            },
            { "field.era", "Era" },
            { "field.year", "Ano" },
            { "field.month", "M\u00eas" },
            { "field.week", "Semana" },
            { "field.weekday", "Dia da semana" },
            { "field.dayperiod", "Per\u00edodo do dia" },
            { "field.hour", "Hora" },
            { "field.minute", "Minuto" },
            { "field.second", "Segundo" },
            { "field.zone", "Fuso" },
            { "TimePatterns",
                new String[] {
                    "HH'h'mm'min'ss's' zzzz",
                    "HH'h'mm'min'ss's' z",
                    "HH:mm:ss",
                    "HH:mm",
                }
            },
            { "DatePatterns",
                new String[] {
                    "EEEE, d 'de' MMMM 'de' y",
                    "d 'de' MMMM 'de' y",
                    "dd/MM/yyyy",
                    "dd/MM/yy",
                }
            },
            { "java.time.buddhist.DatePatterns",
                new String[] {
                    "EEEE, d 'de' MMMM 'de' y G",
                    "d 'de' MMMM 'de' y G",
                    "dd/MM/yyyy G",
                    "d/M/yyyy",
                }
            },
            { "buddhist.DatePatterns",
                new String[] {
                    "EEEE, d 'de' MMMM 'de' y GGGG",
                    "d 'de' MMMM 'de' y GGGG",
                    "dd/MM/yyyy GGGG",
                    "d/M/yyyy",
                }
            },
            { "java.time.japanese.DatePatterns",
                new String[] {
                    "EEEE, d MMMM y G",
                    "d 'de' MMMM 'de' y G",
                    "d MMM y G",
                    "d/M/yy",
                }
            },
            { "japanese.DatePatterns",
                new String[] {
                    "EEEE, d MMMM y GGGG",
                    "d 'de' MMMM 'de' y GGGG",
                    "d MMM y GGGG",
                    "d/M/yy",
                }
            },
            { "roc.Eras",
                new String[] {
                    "Antes de R.O.C.",
                    "R.O.C.",
                }
            },
            { "java.time.roc.DatePatterns",
                new String[] {
                    "EEEE, d 'de' MMMM 'de' y G",
                    "d 'de' MMMM 'de' y G",
                    "dd/MM/yyyy G",
                    "d/M/yyyy",
                }
            },
            { "roc.DatePatterns",
                new String[] {
                    "EEEE, d 'de' MMMM 'de' y GGGG",
                    "d 'de' MMMM 'de' y GGGG",
                    "dd/MM/yyyy GGGG",
                    "d/M/yyyy",
                }
            },
            { "java.time.islamic.DatePatterns",
                new String[] {
                    "EEEE, d 'de' MMMM 'de' y G",
                    "d 'de' MMMM 'de' y G",
                    "dd/MM/yyyy G",
                    "d/M/yyyy",
                }
            },
            { "islamic.DatePatterns",
                new String[] {
                    "EEEE, d 'de' MMMM 'de' y GGGG",
                    "d 'de' MMMM 'de' y GGGG",
                    "dd/MM/yyyy GGGG",
                    "d/M/yyyy",
                }
            },
            { "calendarname.islamic-civil", "Calend\u00e1rio Civil Isl\u00e2mico" },
            { "calendarname.islamicc", "Calend\u00e1rio Civil Isl\u00e2mico" },
            { "calendarname.buddhist", "Calend\u00e1rio Budista" },
            { "calendarname.islamic", "Calend\u00e1rio Isl\u00e2mico" },
            { "calendarname.gregorian", "Calend\u00e1rio Gregoriano" },
            { "calendarname.gregory", "Calend\u00e1rio Gregoriano" },
            { "calendarname.roc", "Calend\u00e1rio da Rep\u00fablica da China" },
            { "calendarname.japanese", "Calend\u00e1rio Japon\u00eas" },
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
                    "\u00a4#,##0.00;(\u00a4#,##0.00)",
                    "#,##0%",
                }
            },
        };
        return data;
    }
}
