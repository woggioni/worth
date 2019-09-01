package net.woggioni.worth.cli

import java.io._

import com.beust.jcommander.{IStringConverter, JCommander, Parameter, ParameterException}
import net.woggioni.worth.serialization.binary.{JBONDumper, JBONParser}
import net.woggioni.worth.serialization.json.{JSONDumper, JSONParser}
import net.woggioni.worth.xface.Value

sealed abstract class SerializationFormat(val name : String) {
    override def toString = name
}

object SerializationFormat {
    case object JSON extends SerializationFormat("json")
    case object JBON extends SerializationFormat("jbon")

    def parse(value : String) : SerializationFormat = {
        value match {
            case JBON.name => JBON
            case JSON.name => JSON
            case _ => {
                val arr = Stream(
                    SerializationFormat.JSON,
                    SerializationFormat.JBON
                ).map(_.name).toArray
                val availableValues = String.join(", ", arr :_*)
                throw new IllegalArgumentException(
                    s"Unknown serialization format '$value', possible values are $availableValues")
            }
        }
    }
}

class OutputTypeConverter extends IStringConverter[SerializationFormat] {
    override def convert(value: String): SerializationFormat = SerializationFormat.parse(value)
}

class CliArg {

    @Parameter(names = Array("-f", "--file"), description = "Name of the .cz file to parse")
    var fileName : String = _

    @Parameter(names = Array("--input-type"), description = "Input type", converter = classOf[OutputTypeConverter])
    var inputType : SerializationFormat = SerializationFormat.JSON

    @Parameter(names = Array("-o", "--output"), description = "Name of the JSON file to generate")
    var outPut : String = _

    @Parameter(names = Array("-t", "--type"), description = "Output type", converter = classOf[OutputTypeConverter])
    var outputType : SerializationFormat = SerializationFormat.JSON

    @Parameter(names = Array("-h", "--help"), help = true)
    var help : Boolean = _
}


object Main {

    def main(argv : Array[String]): Unit = {
        val cliArg = new CliArg
        val cliArgumentParser = JCommander.newBuilder()
            .addObject(cliArg)
            .build()
        try {
            cliArgumentParser.parse(argv :_*)
        } catch {
            case _ : ParameterException => {
                cliArgumentParser.usage()
                System.exit(-1)
            }
            case e : Throwable => throw e
        }
        if(cliArg.help) {
            cliArgumentParser.usage()
            System.exit(0)
        }
        val cfg = Value.Configuration.builder().serializeReferences(true).build()
        val inputStream = if(cliArg.fileName != null) {
            new BufferedInputStream(new FileInputStream(cliArg.fileName))
        } else {
            System.in
        }

        val result = cliArg.inputType match {
            case SerializationFormat.JSON => {
                val reader = new InputStreamReader(inputStream)
                try {
                    new JSONParser(cfg).parse(reader)
                } finally {
                    reader.close()
                }
            }
            case SerializationFormat.JBON => {
                try {
                    new JBONParser(cfg).parse(inputStream)
                } finally {
                    inputStream.close()
                }
            }
        }

        val outputStream = if(cliArg.outPut != null) {
            new BufferedOutputStream(new FileOutputStream(cliArg.outPut))
        } else {
            System.out
        }
        cliArg.outputType match {
            case SerializationFormat.JSON => {
                val writer = new OutputStreamWriter(outputStream)
                try {
                    new JSONDumper(cfg).dump(result, writer)
                } finally {
                    writer.close()
                }
            }
            case SerializationFormat.JBON => {
                try {
                    new JBONDumper(cfg).dump(result, outputStream)
                } finally {
                    outputStream.close()
                }
            }
        }
    }
}
