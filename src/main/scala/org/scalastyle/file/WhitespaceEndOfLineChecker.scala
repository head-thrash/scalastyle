// Copyright (C) 2011-2012 the original author or authors.
// See the LICENCE.txt file distributed with this work for additional
// information regarding copyright ownership.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.scalastyle.file

import org.scalastyle.ColumnError
import org.scalastyle.FileChecker
import org.scalastyle.Lines
import org.scalastyle.ScalastyleError

import scala.Array.canBuildFrom

class WhitespaceEndOfLineChecker extends FileChecker {
  val errorKey = "whitespace.end.of.line"
  lazy val ignoreWhitespaceLines = getBoolean("ignoreWhitespaceLines", false)

  private val whitespaces = Set(' ', '\t')
  private val endOfLines = Set('\n', '\r')

  private def endsWithWhitespace(s: String): (Boolean, Int) = {
    val sb = s.reverse

    (for {
      withoutEndOfLines <- Some(sb.zipWithIndex.dropWhile{ case (c: Char, idx: Int) => endOfLines.contains(c) })
      (nextChar, eolIndex) <- withoutEndOfLines.headOption
      if whitespaces.contains(nextChar)
    } yield {
      withoutEndOfLines.dropWhile{ case (c: Char, idx: Int) =>
        whitespaces.contains(c)
      }.headOption.map{ case (c: Char, idx: Int) =>
        (true, s.length() - idx)
      }.getOrElse {
        if (ignoreWhitespaceLines) (false, 0)
        else (true, 0)
      }
    } ).getOrElse( (false, 0) )
  }

  def verify(lines: Lines): List[ScalastyleError] = {
    val errors = for {
      (line, lineIndex) <- lines.lines.zipWithIndex
      (hasWhitespace, whitespaceIndex) = endsWithWhitespace(line.text)
      if hasWhitespace
      if !ignoreWhitespaceLines || line.text.trim.nonEmpty
    } yield {
      ColumnError(lineIndex + 1, whitespaceIndex)
    }

    errors.toList
  }
}
