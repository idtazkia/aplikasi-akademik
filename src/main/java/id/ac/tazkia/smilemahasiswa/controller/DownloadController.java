package id.ac.tazkia.smilemahasiswa.controller;

import fr.opensagres.xdocreport.converter.ConverterTypeTo;
import fr.opensagres.xdocreport.converter.Options;
import fr.opensagres.xdocreport.core.XDocReportException;
import fr.opensagres.xdocreport.core.document.DocumentKind;
import fr.opensagres.xdocreport.document.IXDocReport;
import fr.opensagres.xdocreport.document.registry.XDocReportRegistry;
import fr.opensagres.xdocreport.template.IContext;
import fr.opensagres.xdocreport.template.TemplateEngineKind;
import id.ac.tazkia.smilemahasiswa.entity.Mahasiswa;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Controller
public class DownloadController {
    @Value("classpath:test.odt")
    private Resource templateTest;

    @GetMapping("/download")
    public void testOdt(@RequestParam(name = "mahasiswa") Mahasiswa mahasiswa, HttpServletResponse response){
        try {
            Options options = Options.getFrom(DocumentKind.ODT).to(ConverterTypeTo.PDF);

            InputStream in = templateTest.getInputStream();

            IXDocReport report = XDocReportRegistry.getRegistry()
                    .loadReport(in, TemplateEngineKind.Freemarker);


            IContext context = report.createContext();
            context.put("nama", mahasiswa.getNama());
            context.put("nim", mahasiswa.getNim());
            context.put("judul", mahasiswa.getNama());
            context.put("ipk", mahasiswa.getNim());
            context.put("prodi", mahasiswa.getIdProdi().getNamaProdi());

            Locale indonesia = new Locale("id", "id");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy", indonesia);

            LocalDate tanggal =
                    LocalDate.now(ZoneId.systemDefault());


            String tanggalSekarang = tanggal.format(formatter);

            context.put("tgl", tanggalSekarang);

            response.setHeader("Content-Disposition", "attachment;filename=test.pdf");
            OutputStream out = response.getOutputStream();
            report.convert(context, options, out);
            out.flush();
        } catch (XDocReportException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
