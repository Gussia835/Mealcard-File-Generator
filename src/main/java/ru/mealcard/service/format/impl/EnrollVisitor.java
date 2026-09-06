package ru.mealcard.service.format.impl;

import org.apache.commons.lang3.StringUtils;
import ru.mealcard.Base;
import ru.mealcard.utils.format.Visitor;
import ru.mealcard.service.format.dto.DataForEnrollDTO;
import ru.mealcard.service.format.dto.EnrollDTO;
import ru.mealcard.exception.generation.FileGenerationException;
import ru.mealcard.utils.encoding.FileEncoding;
import ru.mealcard.utils.generate_models.TypeProcedure;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;

public class EnrollVisitor extends Base implements Visitor<DataForEnrollDTO> {

    private static final DateTimeFormatter YYYYMMDD = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter HHMMSS = DateTimeFormatter.ofPattern("HHmmss");
    private static final String CRLF = "\r\n";

    private static final int FIO_LEN = 100;
    private static final int ACCOUNT_LEN = 30;
    private static final int SUMM_LEN = 20;
    private static final int TYPE_LEN = 2;

    @Override
    public void visit(Path targetFile, DataForEnrollDTO dto) {
        info("Writing ENROLL file: {}", targetFile.getFileName());

        int recordCount = 0;
        try (BufferedWriter writer = Files.newBufferedWriter(targetFile, FileEncoding.WINDOWS_1251.getCharset())) {
            writeHeader(writer, dto);

            for (EnrollDTO record : dto.getRecords()) {
                writeRecord(writer, record);
                debug("writting record: {}", record.getFio(), record.getType(), record.getSumm(), record.getAccount());
                recordCount++;
            }

            writeTrailer(writer, recordCount);
            info("File {} written successfully ({} records)", targetFile.getFileName(), recordCount);
        } catch (IOException e) {
            throw new FileGenerationException("Error writing file " + targetFile);
        }
    }

    private void writeHeader(BufferedWriter writer, DataForEnrollDTO dto) throws IOException {
        TypeProcedure procType = dto.getProcType();

        writer.write("H ");
        writer.write(dto.getSendAt().format(YYYYMMDD));
        writer.write(' ');
        writer.write(dto.getSendAt().format(HHMMSS));
        writer.write(' ');
        writer.write(StringUtils.rightPad(procType.getCode(), 9));

        if (procType == TypeProcedure.IN_TIME && dto.getScheduledDateTime() != null) {
            writer.write(dto.getScheduledDateTime().format(YYYYMMDD));
            writer.write(' ');
            writer.write(dto.getScheduledDateTime().format(HHMMSS));
        } else {
            writer.write(StringUtils.repeat(' ', 8));
            writer.write(' ');
            writer.write(StringUtils.repeat(' ', 6));
        }
        writer.write(CRLF);
    }

    private void writeRecord(BufferedWriter writer, EnrollDTO card) throws IOException {
        String fio = StringUtils.rightPad(
                StringUtils.truncate(StringUtils.defaultString(card.getFio()), FIO_LEN), FIO_LEN);
        String account = StringUtils.rightPad(
                StringUtils.truncate(card.getAccount(), ACCOUNT_LEN), ACCOUNT_LEN);
        String type = StringUtils.rightPad(card.getType().getCode(), TYPE_LEN);
        String summ = StringUtils.leftPad(String.valueOf(card.getSumm()), SUMM_LEN);

        writer.write(fio);
        writer.write(account);
        writer.write(type);
        writer.write(summ);
        writer.write(CRLF);
    }

    private void writeTrailer(BufferedWriter writer, int count) throws IOException {
        writer.write("T");
        writer.write(StringUtils.repeat(' ', 9));
        writer.write(StringUtils.leftPad(String.valueOf(count), 10));
        writer.write(CRLF);
    }
}