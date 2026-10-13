package dev.enzosantos.docbox.applications.ports;

import java.io.InputStream;
import java.util.zip.ZipInputStream;

public interface FileStorage {
  void upload(String path, InputStream buffer);

  InputStream download(String path);

  ZipInputStream downloadAll(String path);

  void delete(String path);
}
