package com.krypton.cloud.controller

import lombok.AllArgsConstructor
import com.krypton.cloud.service.file.FileServiceImpl
import org.springframework.http.*
import org.springframework.http.codec.multipart.FilePart
import org.springframework.http.codec.multipart.FormFieldPart
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

import java.util.HashMap

@AllArgsConstructor
@RestController
@RequestMapping("/file")
class FileController(private val fileService : FileServiceImpl) {

    /**
     * upload one file to specified folder path
     *
     * @param filePart  file for save
     * @param path        path to folder where to save file
     * @return http status
     */
    @PostMapping(value = ["/upload/one"])
    fun uploadFile(
            @RequestPart("file") filePart : Mono<FilePart>,
            @RequestPart("path") path : FormFieldPart
    ) : Mono<HttpStatus> = filePart.flatMap { fileService.saveFile(it, path.value()) }

    /**
     * move folder from one location to another
     *
     * @param request containing folder old and new path
     * @return http status
     */
    @PostMapping("/cut")
    fun cutFolder(@RequestBody request : HashMap<String, String>) : HttpStatus = fileService.cutFile(request["oldPath"], request["newPath"])

    /**
     * copy folder to new path
     *
     * @param request request containing  original folder path and path for folder copy
     * @return http status
     */
    @PostMapping("/copy")
    fun copyFolder(@RequestBody request : HashMap<String, String>) : HttpStatus = fileService.copyFile(request["oldPath"], request["newPath"])

    /**
     * @param request file path and new name
     * @return http status
     */
    @PostMapping("/rename")
    fun renameFile(@RequestBody request : HashMap<String, String>) : HttpStatus = fileService.renameFile(request["path"], request["newName"])


    /**
     * @param request file path
     * @return http status
     */
    @PostMapping("/delete")
    fun deleteFile(@RequestBody request : HashMap<String, String>) : HttpStatus = fileService.deleteFile(request["path"])
}