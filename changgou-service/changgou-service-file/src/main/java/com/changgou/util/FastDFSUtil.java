package com.changgou.util;

import com.changgou.file.FastDFSFile;
import org.csource.common.NameValuePair;
import org.csource.fastdfs.*;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * @Description: 文件管理工具类
 * @Author:      Chenzk
 * @CreateDate:  2020/1/3 0003 下午 7:58
 */
public class FastDFSUtil {

    /**
     * 加载Tracker连接信息
     */
    static {
        try {
            String fileName = new ClassPathResource("fdfs_client.conf").getPath();  // 查找classpath下的文件路径
            ClientGlobal.init(fileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 文件上传
     * @return
     */
    public static String[] upload(FastDFSFile fastDFSFile) throws Exception{
        // 附加参数
        NameValuePair[] meta_list = new NameValuePair[1];
        meta_list[0] = new NameValuePair("author", fastDFSFile.getAuthor());

        TrackerServer trackerServer = getTrackerServer();

        StorageClient storageClient = getStorageClient(trackerServer);

        /**
         * 通过StorageClient访问Storage，实现文件上传并获取上传后的文件存储信息
         * 参数：1、上传文件的字节数组   2、文件的扩展名，如：jpg    3、附加参数，如：拍摄地址 北京
         * uploads[0]：文件上传所存储的Storage的组名字 group1
         * uploads[1]：文件存储到Storage上的文件名称 M00/02/44/Zeki.jpg
         */
        String[] uploads = storageClient.upload_file(fastDFSFile.getContent(), fastDFSFile.getExt(), meta_list);
        return uploads;
    }

    /**
     * 获取文件信息
     * @param groupName ：文件的组名 group1
     * @param remoteFileName ：文件的存储路径名称 M00/00/00/Zeki.jpg
     * @return
     */
    public static FileInfo getFile(String groupName, String remoteFileName) throws Exception{
        TrackerServer trackerServer = getTrackerServer();
        StorageClient storageClient = getStorageClient(trackerServer);
        return storageClient.get_file_info(groupName, remoteFileName);
    }

    /**
     * 文件下载
     */
    public static InputStream download(String groupName, String remoteFileName) throws Exception {
        TrackerServer trackerServer = getTrackerServer();
        StorageClient storageClient = getStorageClient(trackerServer);
        byte[] buffer = storageClient.download_file(groupName, remoteFileName);
        return new ByteArrayInputStream(buffer);
    }

    /**
     * 删除文件
     */
    public static void deteleFile(String groupName, String remoteFileName) throws Exception{
        TrackerServer trackerServer = getTrackerServer();
        StorageClient storageClient = getStorageClient(trackerServer);
        storageClient.delete_file(groupName, remoteFileName);
    }

    /**
     * 获取Storage信息
     * @return
     */
    public static StorageServer getStorages() throws Exception {
        TrackerClient trackerClient = new TrackerClient();
        TrackerServer trackerServer = trackerClient.getConnection();
        return trackerClient.getStoreStorage(trackerServer);
    }

    /**
     * 获取Storage的IP和端口信息
     * @return
     */
    public static ServerInfo[] getServerInfo(String groupName, String remoteFileName) throws Exception {
        TrackerClient trackerClient = new TrackerClient();
        TrackerServer trackerServer = trackerClient.getConnection();
        return trackerClient.getFetchStorages(trackerServer, groupName, remoteFileName);
    }

    /**
     * 获取Tracker信息（刚好这里Tracker的端口和Nginx的端口一样都是8080）
     * @return
     */
    public static String getTrackerInfo() throws Exception {
        TrackerServer trackerServer = getTrackerServer();
        int port = ClientGlobal.getG_tracker_http_port();// 8080
        String ip = trackerServer.getInetSocketAddress().getAddress().getHostAddress();
        String url = "http://" + ip + ":" + port;
        return url;
    }

    /**
     * 获取TrackerServer
     */
    public static TrackerServer getTrackerServer() throws Exception {
        // 创建一个访问TrackerServer的客户端对象TrackerClient，以获取连接信息
        TrackerClient trackerClient = new TrackerClient();
        TrackerServer trackerServer = trackerClient.getConnection();
        return trackerServer;
    }

    /**
     * 获取StorageClient
     */
    public static StorageClient getStorageClient(TrackerServer trackerServer) {
        // 通过TrackerServer的连接信息获取Storage的连接信息，创建StorageClient对象存储Storage连接信息
        StorageClient storageClient = new StorageClient(trackerServer, null);
        return storageClient;
    }



    /**
     * 测试功能
     */
    public static void main(String[] args) throws Exception {
        // FileInfo fileInfo = getFile("group1", "M00/00/00/wKjThF4PQumAFLjOAAFqtuzsh44027.ico");
        // System.out.println(fileInfo.getSourceIpAddr());
        // System.out.println(fileInfo.getCrc32());

        // InputStream in = download("group1", "M00/00/00/wKjThF4PQumAFLjOAAFqtuzsh44027.ico");
        // FileOutputStream out = new FileOutputStream("D:/1.jpg");
        // byte[] buffer = new byte[1024];
        // while (in.read(buffer) != -1) {
        //     out.write(buffer);
        // }
        // out.flush();
        // out.close();
        // in.close();

        //deteleFile("group1", "M00/00/00/wKjThF4PYbqAVhYRAAB2zFvYqFE270.jpg");

        // StorageServer storageServer = getStorages();
        // System.out.println(storageServer.getStorePathIndex());
        // System.out.println(storageServer.getInetSocketAddress());

        // ServerInfo[] serverInfo = getServerInfo("group1", "M00/00/00/wKjThF4PZouAd2LFAAB2zFvYqFE801.jpg");
        // for (ServerInfo info : serverInfo) {
        //     System.out.println(info.getIpAddr());
        //     System.out.println(info.getPort());
        // }

        // System.out.println(getTrackerInfo());
    }
}
