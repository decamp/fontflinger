//ttc -> ttf(for Be)s
//  ver.0.00   (c)Beer  2003.4.3
#include "ttc2ttf.h"

#include <stdlib.h>
#include <string>
#include <stdio.h>

void	prepare_dir();
void	del_unused_glyf();	//glyf,loca,hmtx,vmtx
void	remake_cmap();		//cmap
void	update_tbl();		//head,hhea,vhea,maxp
void	copy_tbl();			//OS/2,cvt ,fpgm,gasp,name,post,prep
void	write_dir();		//table_directory,total_sum(head)

uint32	set_tbl(uint32 tblname,uchar *dt,uint32 len);
uchar*	search_newtbl(uint32 tblname);
int		search_tbl(uint32 tblname,uchar *s);
uint32	sumB32(uint32 *p,int len);
void	quit(const char *p);

uchar	ttch[256];
uchar	rbuf[32*1024*1024];	//32MBytes
uchar	wbuf[12*1024*1024];	//12MBytes
char	ofc,ifn[256],ofn[256];
FILE	*tfp,*ifp,*ofp;
uint32	wpos;

uchar	mode;
uchar	ttfn;

uint16	rgc,wgc;			//count of glyf
uint16	gno[65536];			//chr_code->glyf_no
uchar	glff[65536];		//glyf_flag   0.space 1.character
uchar	used[65536];		//mapped_flag   0.unused 1.used

uchar	*tdir,*loca,*glyf;
uchar	*cmap,*hmtx,*vmtx;
uint32	dirl;

main(int ac,char **ag)
{
	if(ac<2)quit("arg error\nusage: ttc2ttf [-x] ttc_file\n\tx=0.simple\n\tx=1.delete unnecessary glyph\n\tx=2.delete space character\n\tx=3.delete table BeOS unused(default)");
	strcpy(ifn,"");
	mode=3;	//3=minimum output
	for(int i=1;i<ac;i++){
		if(ag[i][0]=='-'){
			mode=ag[i][1]-'0';
		}else{
			strcpy(ifn,ag[i]);
		}
	}

	if((ifp=fopen(ifn,"rb"))==NULL)quit("cannot open ttc-file");
	fseek(ifp,0,0);
	fread(ttch,1,12,ifp);
	ofc=ttch[11];
	fread(ttch+12,1,ofc*4,ifp);
	fclose(ifp);

	for(ttfn=0;ttfn<ofc;ttfn++){
		if(ofc==1)sprintf(ofn,"new.ttf");else sprintf(ofn,"new%d.ttf",ttfn);
		if((ofp=fopen(ofn,"wb"))==NULL)quit("cannot output");
		wpos=0;

		tdir=wbuf;				//(0000000-000ffff)64kB
		prepare_dir();

		if(mode>0){
			loca=wbuf+0x40000;		//(0040000-007ffff)256kB
			hmtx=wbuf+0x80000;		//(0080000-00bffff)256kB
			vmtx=wbuf+0xc0000;		//(00c0000-00fffff)256kB
			glyf=wbuf+0x100000;		//(0100000-1ffffff)31MB
			del_unused_glyf();
			if(mode>1){
				cmap=wbuf+0x100000;		//(0100000-)
				remake_cmap();
			}
		}

		update_tbl();
		copy_tbl();
		write_dir();

		fclose(ofp);
	}
	quit("");
}

void prepare_dir()
{
printf("prepare dir\n");
	uchar	*dp=tdir;
	if((ifp=fopen(ifn,"rb"))==NULL)quit("cannot open ttc-file");
	fseek(ifp,B_BENDIAN_TO_HOST_INT32(*(uint32*)(ttch+12+ttfn*4)),0);
	fread(dp,1,12,ifp);
	uchar	*tcp=dp+5;
	int	tbls=*tcp;
	dp+=12;
printf("tbls=%d\n",tbls);
	for(int	j=0;j<tbls;j++){
		fread(dp,1,16,ifp);
		uint32	tn=B_BENDIAN_TO_HOST_INT32(*(uint32*)dp);
		if(mode<3){
			dp+=16;
		}else{
			if((tn=='OS/2')||(tn=='cmap')||(tn=='cvt ')||(tn=='fpgm')||(tn=='gasp')||(tn=='glyf')||(tn=='head')||(tn=='hhea')||(tn=='hmtx')||(tn=='loca')||(tn=='maxp')||(tn=='name')||(tn=='post')||(tn=='prep')||(tn=='vhea')||(tn=='vmtx')){
				dp+=16;
			}else{
				(*tcp)--;
			}
		}
	}
	dirl=dp-tdir;
	//toriaezu//
	wpos+=fwrite(tdir,1,dirl,ofp);
}

void del_unused_glyf()
{
	uchar	s[16];
printf("delete unused glyf\n");
	if((ifp=fopen(ifn,"rb"))==NULL)quit("cannot open ttf-file");
	uchar	*cp,*lp,*gp,*hp,*vp;
	uint32	cl,ll,gl,hl,vl;
	if(search_tbl('cmap',s))quit("table(cmap) is not found");
	cp=rbuf;cl=B_BENDIAN_TO_HOST_INT32(*(uint32*)(s+12));
	fseek(ifp,B_BENDIAN_TO_HOST_INT32(*(uint32*)(s+8)),0);
	fread(cp,1,cl,ifp);
	if(search_tbl('loca',s))quit("table(loca) is not found");
	lp=rbuf+cl;ll=B_BENDIAN_TO_HOST_INT32(*(uint32*)(s+12));
	fseek(ifp,B_BENDIAN_TO_HOST_INT32(*(uint32*)(s+8)),0);
	fread(lp,1,ll,ifp);
	if(search_tbl('glyf',s))quit("table(glyf) is not found");
	gp=rbuf+cl+ll;gl=B_BENDIAN_TO_HOST_INT32(*(uint32*)(s+12));
	fseek(ifp,B_BENDIAN_TO_HOST_INT32(*(uint32*)(s+8)),0);
	fread(gp,1,gl,ifp);
	if(search_tbl('hmtx',s))quit("table(hmtx) is not found");
	hp=rbuf+cl+ll+gl;hl=B_BENDIAN_TO_HOST_INT32(*(uint32*)(s+12));
	fseek(ifp,B_BENDIAN_TO_HOST_INT32(*(uint32*)(s+8)),0);
	fread(hp,1,hl,ifp);
	for(int i=hl;i<ll-4;i++)hp[i]=hp[i-4];
	if(search_tbl('vmtx',s))quit("table(vmtx) is not found");
	vp=rbuf+cl+ll+gl+ll-4;vl=B_BENDIAN_TO_HOST_INT32(*(uint32*)(s+12));
	fseek(ifp,B_BENDIAN_TO_HOST_INT32(*(uint32*)(s+8)),0);
	fread(vp,1,vl,ifp);
	for(int i=vl;i<ll-4;i++)vp[i]=vp[i-4];
	fclose(ifp);
//search unused glyf
	int	tbc=cp[3];
	uchar	*cmp;
	int	j;
	for(j=0;j<tbc;j++){
		cmp=rbuf+B_BENDIAN_TO_HOST_INT32(*(uint32*)(cp+4+j*8+4));
		if(cmp[1]==4)break;	//cp=table_format4
	}
	if(j==tbc)quit("table(cmap) don't have format4");

	used[0]=2;for(int i=1;i<65536;i++)used[i]=0;
	for(int c=0;c<65536;c++)gno[c]=0;

	uint16	sc=B_BENDIAN_TO_HOST_INT16(*(uint16*)(cmp+6));	//segCountX2
	uint16	*ecp=(uint16*)(cmp+14);							//endCount[segCount]
	uint16	*scp=(uint16*)(cmp+sc+16);						//startCount[segCount]
	uint16	*idp=(uint16*)(cmp+sc*2+16);					//idDelta[segCount]
	uint16	*irp=(uint16*)(cmp+sc*3+16);					//idRangeOffset[segCount]
	uint16	*gap=(uint16*)(cmp+sc*4+16);					//glyphIdArray[]
	for(j=0;j<sc/2;j++){
		if(irp[j]==0){
			for(int c=B_BENDIAN_TO_HOST_INT16(scp[j]);c<=B_BENDIAN_TO_HOST_INT16(ecp[j]);c++){
				uint16	cc=c+B_BENDIAN_TO_HOST_INT16(idp[j]);
				gno[c]=cc;
				used[cc]|=1;
				if((c==0x0020)||(c==0x3000))used[cc]|=2;
			}
		}else{
			printf("not-supported GlyphIdArray\n [skip] chr=%04x-%04x delta=%04x offset=%04x\n",B_BENDIAN_TO_HOST_INT16(scp[j]),B_BENDIAN_TO_HOST_INT16(ecp[j]),B_BENDIAN_TO_HOST_INT16(idp[j]),B_BENDIAN_TO_HOST_INT16(irp[j]));
		}
	}
//make_new_loca,glyf
	uint32	ngno[65536];
	int	gc=ll/4-1;
	wgc=0;
	uchar	*wgp=glyf;
	uint32	*wlp=(uint32*)loca;
	uint32	*whp=(uint32*)hmtx;
	uint32	*wvp=(uint32*)vmtx;
	*(wlp++)=0;
	for(int i=0;i<gc;i++){
		uint32	len=used[i]?B_BENDIAN_TO_HOST_INT32(*((uint32*)lp+i+1))-B_BENDIAN_TO_HOST_INT32(*((uint32*)lp+i)):0;
		if(mode>1)if((used[i]==0)||((len==0)&&(used[i]==1))){
			ngno[i]=0;
			continue;
		}
		uchar	*rgp=gp+B_BENDIAN_TO_HOST_INT32(*((uint32*)lp+i));
		if(mode>1)if(*rgp&0x80){
			printf("not-supported CompositeGlyph\n [skip] glfno=%04x\n",i);
			ngno[i]=0;
			continue;
		}
		for(int j=0;j<len;j++)*(wgp++)=*(rgp++);
		*(whp++)=*((uint32*)hp+i);
		*(wvp++)=*((uint32*)vp+i);
		*(wlp++)=B_HOST_TO_BENDIAN_INT32(wgp-glyf);
		ngno[i]=wgc++;
	}
	for(int c=0;c<65536;c++)gno[c]=ngno[gno[c]];
printf("output_glyfs=%04x\n",wgc);
//output loca,glyf,hmtx,vmtx
	uint32	len;
	len=set_tbl('loca',loca,(wgc+1)*4);
	wpos+=fwrite(loca,1,len,ofp);
	len=set_tbl('glyf',glyf,wgp-glyf);
	wpos+=fwrite(glyf,1,len,ofp);
	if(mode>1){
		len=set_tbl('hmtx',hmtx,wgc*4);
		wpos+=fwrite(hmtx,1,len,ofp);
		len=set_tbl('vmtx',vmtx,wgc*4);
		wpos+=fwrite(vmtx,1,len,ofp);
	}
}

void remake_cmap()
{
//create_cmap
printf("create_cmap\n");
	uchar	*cp=cmap;
	*(uint16*)(cp+0)=B_HOST_TO_BENDIAN_INT16(0);	//table_version
	*(uint16*)(cp+2)=B_HOST_TO_BENDIAN_INT16(1);	//number_of_tables
	cp+=4;
	uchar	*cmp=cp+8;
	*(uint16*)(cp+0)=B_HOST_TO_BENDIAN_INT16(3);	//plathome_ID
	*(uint16*)(cp+2)=B_HOST_TO_BENDIAN_INT16(1);	//plathome_specific_encoding_ID
	*(uint32*)(cp+4)=B_HOST_TO_BENDIAN_INT32(cmp-cmap);	//offset

	uint16	sc,sr,es;
	uint16	*ecp,*scp,*idp,*irp,*gap;
	sc=1;sr=1;es=(uint16)-1;
	for(int i=1;i<65536;i++)if((gno[i])&&((gno[i]-gno[i-1]!=1)||(gno[i]==1)))sc++;
printf("segcount=%04x\n",sc);
	if(sc>0x1ffc)quit("cmap_segcount is too big\n");
	while(sr<sc){sr<<=1;es++;}
	sc*=2;
	*(uint16*)(cmp+0)=B_HOST_TO_BENDIAN_INT16(4);		//format=4(Segment mapping)
	*(uint16*)(cmp+2)=B_HOST_TO_BENDIAN_INT16(sc*4+16);	//length
	*(uint16*)(cmp+4)=B_HOST_TO_BENDIAN_INT16(0);		//version
	*(uint16*)(cmp+6)=B_HOST_TO_BENDIAN_INT16(sc);		//segCountX2
	*(uint16*)(cmp+8)=B_HOST_TO_BENDIAN_INT16(sr);		//searchRange
	*(uint16*)(cmp+10)=B_HOST_TO_BENDIAN_INT16(es);		//entrySelector
	*(uint16*)(cmp+12)=B_HOST_TO_BENDIAN_INT16(sc-sr);	//rangeShift
	ecp=(uint16*)(cmp+14);					//endCount[segCount]
	*(uint16*)(cmp+14+sc)=B_HOST_TO_BENDIAN_INT16(0);	//reserved
	scp=(uint16*)(cmp+16+sc);				//startCount[segCount]
	idp=(uint16*)(cmp+16+sc*2);				//idDelta[segCount]
	irp=(uint16*)(cmp+16+sc*3);				//idRangeOffset[segCount]
//	gap=(uint16*)(cmp+16+sc*4);				//glyphIdArray[0](no data)

	for(int i=1;i<65536;i++){
		if(gno[i]==0)continue;
		*(scp++)=B_HOST_TO_BENDIAN_INT16(i);
		*(idp++)=B_HOST_TO_BENDIAN_INT16(gno[i]-i);
		*(irp++)=B_HOST_TO_BENDIAN_INT16(0);
		while(gno[i+1]-gno[i]==1)i++;
		*(ecp++)=B_HOST_TO_BENDIAN_INT16(i);
	}
	*(scp++)=B_HOST_TO_BENDIAN_INT16(0xffff);
	*(idp++)=B_HOST_TO_BENDIAN_INT16(1);
	*(irp++)=B_HOST_TO_BENDIAN_INT16(0);
	*(ecp++)=B_HOST_TO_BENDIAN_INT16(0xffff);

	uint32	len=(cmp-cmap)+(sc*4+16);
	len=set_tbl('cmap',cmap,len);
	wpos+=fwrite(cmap,1,len,ofp);
}

void update_tbl()
{
	uchar	s[16];
	uint32	len;

printf("update_tbl\n");
	if((ifp=fopen(ifn,"rb"))==NULL)quit("cannot open ttf-file");
//head
	if(search_tbl('head',s))quit("table(head) is not found");
	len=B_BENDIAN_TO_HOST_INT32(*(uint32*)(s+12));
	fseek(ifp,B_BENDIAN_TO_HOST_INT32(*(uint32*)(s+8)),0);
	fread(rbuf,1,len,ifp);
	*(uint32*)(rbuf+8)=0;								//checkSumAdjustment
	len=set_tbl('head',rbuf,len);
	wpos+=fwrite(rbuf,1,len,ofp);

	if(mode>1){
//hhea
		if(search_tbl('hhea',s))quit("table(hhea) is not found");
		len=B_BENDIAN_TO_HOST_INT32(*(uint32*)(s+12));
		fseek(ifp,B_BENDIAN_TO_HOST_INT32(*(uint32*)(s+8)),0);
		fread(rbuf,1,len,ifp);
		*(uint16*)(rbuf+34)=B_HOST_TO_BENDIAN_INT16(wgc);		//numberOfHMetrics
		len=set_tbl('hhea',rbuf,len);
		wpos+=fwrite(rbuf,1,len,ofp);
//vhea
		if(search_tbl('vhea',s))quit("table(vhea) is not found");
		len=B_BENDIAN_TO_HOST_INT32(*(uint32*)(s+12));
		fseek(ifp,B_BENDIAN_TO_HOST_INT32(*(uint32*)(s+8)),0);
		fread(rbuf,1,len,ifp);
		*(uint16*)(rbuf+34)=B_HOST_TO_BENDIAN_INT16(wgc);		//numberOfVMetrics
		len=set_tbl('vhea',rbuf,len);
		wpos+=fwrite(rbuf,1,len,ofp);
//maxp
		if(search_tbl('maxp',s))quit("table(maxp) is not found");
		len=B_BENDIAN_TO_HOST_INT32(*(uint32*)(s+12));
		fseek(ifp,B_BENDIAN_TO_HOST_INT32(*(uint32*)(s+8)),0);
		fread(rbuf,1,len,ifp);
		*(uint16*)(rbuf+4)=B_HOST_TO_BENDIAN_INT16(wgc);		//numGlyphs
		len=set_tbl('maxp',rbuf,len);
		wpos+=fwrite(rbuf,1,len,ofp);
	}
	fclose(ifp);
}

void copy_tbl()
{
printf("copy_tbl\n");
	if((ifp=fopen(ifn,"rb"))==NULL)quit("cannot open ttf-file");
	int	tbls=tdir[5];
	for(int i=0;i<tbls;i++){
		uchar	*p=tdir+12+i*16;
		uint32	tn=B_BENDIAN_TO_HOST_INT32(*(uint32*)p);
		if(tn=='head')continue;
		if(mode>0)if((tn=='glyf')||(tn=='loca'))continue;
		if(mode>1)if((tn=='cmap')||(tn=='hhea')||(tn=='hmtx')||(tn=='maxp')||(tn=='vhea')||(tn=='vmtx'))continue;
		uint32	len=B_BENDIAN_TO_HOST_INT32(*(uint32*)(p+12));
		fseek(ifp,B_BENDIAN_TO_HOST_INT32(*(uint32*)(p+8)),0);
		fread(rbuf,1,len,ifp);
		len=set_tbl(tn,rbuf,len);
		wpos+=fwrite(rbuf,1,len,ofp);
	}
	fclose(ifp);
}

void write_dir()
{
	uchar	s[16];
	fseek(ofp,0,0);
	fwrite(tdir,1,dirl,ofp);
//total_sum(head)
	uchar	*p=tdir;
	uchar	tbls=p[5];
	uint32	sum=sumB32((uint32*)p,12+tbls*16);
	p+=12;
	for(int j=0;j<tbls;j++){
		sum+=B_BENDIAN_TO_HOST_INT32(*(uint32*)(p+j*16+4));
	}
printf("write_sum(head)\n");
	sum=B_HOST_TO_BENDIAN_INT32(0xb1b0afba-sum);
	p=search_newtbl('head')+8;
	fseek(ofp,B_BENDIAN_TO_HOST_INT32(*(uint32*)p)+8,0);
	fwrite(&sum,4,1,ofp);
}

uint32 set_tbl(uint32 tblname,uchar *dt,uint32 len)
{
	while(len&3)dt[len++]=0;
	uint32	*p=(uint32*)search_newtbl(tblname);
	uint32	sum=sumB32((uint32*)dt,len);
	*(++p)=B_HOST_TO_BENDIAN_INT32(sum);	//sum
	*(++p)=B_HOST_TO_BENDIAN_INT32(wpos);	//offset
	*(++p)=B_HOST_TO_BENDIAN_INT32(len);	//length
	return(len);
}

uchar* search_newtbl(uint32 tblname)
{
	uchar	*r=NULL;
	uchar	*p=tdir;
	int	tbls=p[5];
	p+=12;
	for(int i=0;i<tbls;i++){
		if(B_BENDIAN_TO_HOST_INT32(*(uint32*)p)==tblname){r=p;break;}
		p+=16;
	}
	return(r);
}

int search_tbl(uint32 tblname,uchar *s)
{
	int	r=1;
	fseek(ifp,B_BENDIAN_TO_HOST_INT32(*(uint32*)(ttch+12+ttfn*4)),0);
	fread(s,1,12,ifp);
	int	tbls=s[5];
	for(int i=0;i<tbls;i++){
		fread(s,1,16,ifp);
		if(B_BENDIAN_TO_HOST_INT32(*(uint32*)s)==tblname){r=0;break;}
	}
	return(r);
}

uint32 sumB32(uint32 *p,int len)
{
	uint32	sum=0;
	len=len/4;
	while(len--)sum+=B_BENDIAN_TO_HOST_INT32(*(p++));
	return(sum);
}

void quit(const char *p)
{
	if(*p)printf("error : %s\n",p);
	else printf("complete\n");
	exit(0);
}
