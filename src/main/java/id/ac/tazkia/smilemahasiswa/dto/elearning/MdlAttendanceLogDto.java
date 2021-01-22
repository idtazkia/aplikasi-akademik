package id.ac.tazkia.smilemahasiswa.dto.elearning;

import lombok.Data;


import java.math.BigInteger;

@Data
public class MdlAttendanceLogDto {

    private BigInteger id;
    private String sessionid;

    private String studentid;

    private String statusid;

    private String statusset;

    private BigInteger timetaken;
    private String takenby;

    private String remarks;

    private String ipaddress;

    private String statusimport;

}
