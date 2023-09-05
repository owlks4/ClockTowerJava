package com.clockTower.fileUtilities;


//This is the FFMPEG method. TODO: convert into java

public class MicrosoftVideo1 {
    /*
     * Microsoft Video-1 Decoder
     * Copyright (c) 2003 The FFmpeg Project
     *
     * This file is part of FFmpeg.
     *
     * FFmpeg is free software; you can redistribute it and/or
     * modify it under the terms of the GNU Lesser General Public
     * License as published by the Free Software Foundation; either
     * version 2.1 of the License, or (at your option) any later version.
     *
     * FFmpeg is distributed in the hope that it will be useful,
     * but WITHOUT ANY WARRANTY; without even the implied warranty of
     * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
     * Lesser General Public License for more details.
     *
     * You should have received a copy of the GNU Lesser General Public
     * License along with FFmpeg; if not, write to the Free Software
     * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
     *

     *
     * @file
     * Microsoft Video-1 Decoder by Mike Melanson (melanson@pcisys.net)
     * For more information about the MS Video-1 format, visit:
     *   http://www.pcisys.net/~melanson/codecs/
     *
     */
        /*
        int PALETTE_COUNT = 256;


       public static void CHECK_STREAM_PTR(int peek_amount) { 
       if ((stream_ptr + n) > s->size ) { 
         av_log(s->avctx, AV_LOG_ERROR, " MS Video-1 warning: stream_ptr out of bounds (%d >= %d)\n", \
           stream_ptr + n, s->size); 
        return; 
        } 
       }
 
public class Msvideo1Context
    {
     public publicAVCodecContext avctx;
     public AVFrame frame;

            public byte[] buf;
            public int size;

            public int mode_8bit;   // if it's not 8-bit, it's 16-bit 

            public Color[] pal = new Color[256];
    }


 public static int msvideo1_decode_init(AVCodecContext avctx)
 {
     Msvideo1Context s = avctx.priv_data;
 
    s.avctx = avctx;

    // figure out the colorspace based on the presence of a palette 
     if (s.avctx.bits_per_coded_sample == 8) {
         s.mode_8bit = 1;
         avctx.pix_fmt = AV_PIX_FMT_PAL8;
         if (avctx.extradata_size >= AVPALETTE_SIZE)
             memcpy(s.pal, avctx.extradata, AVPALETTE_SIZE);
     } else {
          s.mode_8bit = 0;
         avctx.pix_fmt = AV_PIX_FMT_RGB555;
     }
 
     s.frame = av_frame_alloc();
     if (!s.frame)
         return AVERROR(ENOMEM);

      return 0;
 }
  
 public static void msvideo1_decode_8bit(Msvideo1Context s)
{
    int block_ptr, pixel_ptr;
    int total_blocks;
    int pixel_x, pixel_y;  // pixel width and height iterators 
    int block_x, block_y;  // block width and height iterators 
    int blocks_wide, blocks_high;  // width and height in 4x4 blocks 
    int block_inc;
    int row_dec;

    // decoding parameters
    int stream_ptr;
    byte byte_a, byte_b;
    short flags;
    int skip_blocks;
    byte[] colors = new byte[8];
    byte[] pixels = s.frame.data[0];
    int stride = s.frame.linesize[0];

    stream_ptr = 0;
    skip_blocks = 0;
    blocks_wide = s.avctx.width / 4;
    blocks_high = s.avctx.height / 4;
    total_blocks = blocks_wide * blocks_high;
    block_inc = 4;
    row_dec = stride + 4;

    for (block_y = blocks_high; block_y > 0; block_y--) {
      block_ptr = ((block_y * 4) - 1) * stride;
       for (block_x = blocks_wide; block_x > 0; block_x--) {
             // check if this block should be skipped 
             if (skip_blocks > 0) {
                 block_ptr += block_inc;
                 skip_blocks--;
                total_blocks--;
                 continue;
             }

             pixel_ptr = block_ptr;
 
             // get the next two bytes in the encoded data stream 
             CHECK_STREAM_PTR(2);
             byte_a = s.buf[stream_ptr++];
            byte_b = s.buf[stream_ptr++];

            // check if the decode is finished 
             if ((byte_a == 0) && (byte_b == 0) && (total_blocks == 0))
                 return;
             else if ((byte_b & 0xFC) == 0x84) {
                 // skip code, but don't count the current block 
                 skip_blocks = ((byte_b - 0x84) << 8) + byte_a - 1;
            } else if (byte_b< 0x80) {
                // 2-color encoding 
                flags = (byte_b << 8) | byte_a;

                 CHECK_STREAM_PTR(2);
                 colors[0] = s.buf[stream_ptr++];
                 colors[1] = s.buf[stream_ptr++];
 
                for (pixel_y = 0; pixel_y< 4; pixel_y++) {
                     for (pixel_x = 0; pixel_x < 4; pixel_x++, flags >>>= 1) { 
                         pixels[pixel_ptr++] = colors[(flags & 0x1) ^ 1];
                            }
                     pixel_ptr -= row_dec;
                 }
             } else if (byte_b >= 0x90) {
                 // 8-color encoding 
                 flags = (byte_b << 8) | byte_a;
 
                 CHECK_STREAM_PTR(8);
                 memcpy(colors, s.buf[stream_ptr], 8);
                stream_ptr += 8;

                for (pixel_y = 0; pixel_y< 4; pixel_y++) {
                      for (pixel_x = 0; pixel_x < 4; pixel_x++, flags >>>= 1){
                            pixels[pixel_ptr++] = colors[((pixel_y & 0x2) << 1) + (pixel_x & 0x2) + ((flags & 0x1) ^ 1)];
                            }
                    pixel_ptr -= row_dec;
              }
             } else {
                 // 1-color encoding 
                 colors[0] = byte_a;
 
                 for (pixel_y = 0; pixel_y< 4; pixel_y++) {
                     for (pixel_x = 0; pixel_x< 4; pixel_x++)
                         pixels[pixel_ptr++] = colors[0];
                     pixel_ptr -= row_dec;
                 }
             }

             block_ptr += block_inc;
             total_blocks--;
        }
     }
 
    // make the palette available on the way out 
     if (s.avctx.pix_fmt == AV_PIX_FMT_PAL8)
         memcpy(s.frame->data[1], s.pal, AVPALETTE_SIZE);
 }

static void msvideo1_decode_16bit(Msvideo1Context s)
{
    int block_ptr, pixel_ptr;
    int total_blocks;
    int pixel_x, pixel_y;  // pixel width and height iterators 
    int block_x, block_y;  // block width and height iterators 
    int blocks_wide, blocks_high;  // width and height in 4x4 blocks 
    int block_inc;
    int row_dec;

     // decoding parameters 
    int stream_ptr;
    byte byte_a, byte_b;
    short flags;
     int skip_blocks;
     short[] colors = new short[8];
     short[] pixels = (short[])s.frame.data[0];
     int stride = s.frame.linesize[0] / 2;
 201 
 202     stream_ptr = 0;
 203     skip_blocks = 0;
 204     blocks_wide = s->avctx->width / 4;
 205     blocks_high = s->avctx->height / 4;
 206     total_blocks = blocks_wide* blocks_high;
 207     block_inc = 4;
 208     row_dec = stride + 4;
 209 
 210     for (block_y = blocks_high; block_y > 0; block_y--) {
 211         block_ptr = ((block_y* 4) - 1) * stride;
 212         for (block_x = blocks_wide; block_x > 0; block_x--) {
 213             // check if this block should be skipped
 214             if (skip_blocks) {
 215                 block_ptr += block_inc;
 216                 skip_blocks--;
 217                 total_blocks--;
 218                 continue;
 219             }
 220 
 221             pixel_ptr = block_ptr;
 222 
 223             // get the next two bytes in the encoded data stream 
 224             CHECK_STREAM_PTR(2);
 225             byte_a = s->buf[stream_ptr++];
 226             byte_b = s->buf[stream_ptr++];
 227 
 228             // check if the decode is finished 
 229             if ((byte_a == 0) && (byte_b == 0) && (total_blocks == 0)) {
 230                 return;
 231             } else if ((byte_b & 0xFC) == 0x84) {
 232                 // skip code, but don't count the current block
 233                 skip_blocks = ((byte_b - 0x84) << 8) + byte_a - 1;
 234             } else if (byte_b< 0x80) {
 235                 // 2- or 8-color encoding modes
 236                 flags = (byte_b << 8) | byte_a;
 237 
 238                 CHECK_STREAM_PTR(4);
 239                 colors[0] = AV_RL16(&s->buf[stream_ptr]);
 240                 stream_ptr += 2;
 241                 colors[1] = AV_RL16(&s->buf[stream_ptr]);
 242                 stream_ptr += 2;
 243 
 244                 if (colors[0] & 0x8000) {
 245                     // 8-color encoding 
 246                     CHECK_STREAM_PTR(12);
 247                     colors[2] = AV_RL16(&s->buf[stream_ptr]);
 248                     stream_ptr += 2;
 249                     colors[3] = AV_RL16(&s->buf[stream_ptr]);
 250                     stream_ptr += 2;
 251                     colors[4] = AV_RL16(&s->buf[stream_ptr]);
 252                     stream_ptr += 2;
 253                     colors[5] = AV_RL16(&s->buf[stream_ptr]);
 254                     stream_ptr += 2;
 255                     colors[6] = AV_RL16(&s->buf[stream_ptr]);
 256                     stream_ptr += 2;
 257                     colors[7] = AV_RL16(&s->buf[stream_ptr]);
 258                     stream_ptr += 2;
 259 
 260                     for (pixel_y = 0; pixel_y< 4; pixel_y++) {
 261                         for (pixel_x = 0; pixel_x< 4; pixel_x++, flags >>>= 1)
 262                             pixels[pixel_ptr++] =
 263                                 colors[((pixel_y & 0x2) << 1) +
 264(pixel_x & 0x2) + ((flags & 0x1) ^ 1)];
 265                         pixel_ptr -= row_dec;
 266                     }
 267                 } else {
 268                     // 2-color encoding
 269                     for (pixel_y = 0; pixel_y< 4; pixel_y++) {
 270                         for (pixel_x = 0; pixel_x< 4; pixel_x++, flags >>>= 1)
 271                             pixels[pixel_ptr++] = colors[(flags & 0x1) ^ 1];
 272                         pixel_ptr -= row_dec;
 273                     }
 274                 }
 275             } else {
 276                 // otherwise, it's a 1-color block 
 277                 colors[0] = (byte_b << 8) | byte_a;
 278 
 279                 for (pixel_y = 0; pixel_y< 4; pixel_y++) {
 280                     for (pixel_x = 0; pixel_x< 4; pixel_x++)
 281                         pixels[pixel_ptr++] = colors[0];
 282                     pixel_ptr -= row_dec;
 283                 }
 284             }
 285 
 286             block_ptr += block_inc;
 287             total_blocks--;
 288         }
 289     }
 290 }
 291 
 292 static int msvideo1_decode_frame(AVCodecContext* avctx,
 293                                 void* data, int* got_frame,
 294                                 AVPacket* avpkt)
 295 {
 296     const uint8_t* buf = avpkt->data;
 297     int buf_size = avpkt->size;
 298     Msvideo1Context* s = avctx->priv_data;
 299     int ret;
 300 
 301     s->buf = buf;
 302     s->size = buf_size;
 303 
 304     if ((ret = ff_reget_buffer(avctx, s->frame)) < 0)
 305         return ret;
 306 
 307     if (s->mode_8bit) {
 308         const uint8_t* pal = av_packet_get_side_data(avpkt, AV_PKT_DATA_PALETTE, NULL);
 309 
 310         if (pal) {
 311             memcpy(s->pal, pal, AVPALETTE_SIZE);
 312             s->frame->palette_has_changed = 1;
 313         }
 314     }
 315 
 316     if (s->mode_8bit)
 317         msvideo1_decode_8bit(s);
 318     else
 319         msvideo1_decode_16bit(s);
 320 
 321     if ((ret = av_frame_ref(data, s->frame)) < 0)
 322         return ret;
 323 
 324     * got_frame = 1;
 325 
 326     // report that the buffer was completely consumed
 327     return buf_size;
 328 }
 329 
 330 static av_cold int msvideo1_decode_end(AVCodecContext* avctx)
 331 {
 332     Msvideo1Context* s = avctx->priv_data;
 333 
 334     av_frame_free(&s->frame);
 335 
 336     return 0;
 337 }
 338 
 339 AVCodec ff_msvideo1_decoder = {
 340     .name           = "msvideo1",
 341     .long_name      = NULL_IF_CONFIG_SMALL("Microsoft Video 1"),
 342     .type           = AVMEDIA_TYPE_VIDEO,
 343     .id             = AV_CODEC_ID_MSVIDEO1,
 344     .priv_data_size = sizeof(Msvideo1Context),
 345     .init           = msvideo1_decode_init,
 346     .close          = msvideo1_decode_end,
 347     .decode         = msvideo1_decode_frame,
 348     .capabilities   = AV_CODEC_CAP_DR1,
 349 };*/
}
