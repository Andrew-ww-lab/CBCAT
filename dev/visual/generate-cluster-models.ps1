$ErrorActionPreference = 'Stop'
$utf8 = [Text.UTF8Encoding]::new($false)
Add-Type -AssemblyName System.IO.Compression.FileSystem
$arc = [IO.Compression.ZipFile]::OpenRead((Join-Path $PWD 'libs/cbc_at_Neoforge_1.21.1_0.1.4c.jar'))
function Read-Asset($path) { $r=[IO.StreamReader]::new($arc.GetEntry($path).Open()); try { $r.ReadToEnd()|ConvertFrom-Json } finally {$r.Dispose()} }
function Write-Asset($path,$data) { $dest=Join-Path $PWD "src/main/resources/$path"; [IO.Directory]::CreateDirectory([IO.Path]::GetDirectoryName($dest))|Out-Null; [IO.File]::WriteAllText($dest,($data|ConvertTo-Json -Depth 35),$utf8) }
function Quad($face,$u0,$y0,$u1,$y1,$texture,$uv) {
    switch($face) {
        north {$a=@($u0,$y0,3);$b=@($u1,$y1,3)}
        south {$a=@($u0,$y0,13);$b=@($u1,$y1,13)}
        west {$a=@(3,$y0,$u0);$b=@(3,$y1,$u1)}
        east {$a=@(13,$y0,$u0);$b=@(13,$y1,$u1)}
    }
    @{from=$a;to=$b;faces=@{$face=@{texture=$texture;uv=$uv}}}
}
try {
    foreach($form in @('cluster','cluster_cartridge','cluster_caseless')) {
        $path=if($form -eq 'cluster'){'cluster_block'}else{"projectile_cartridge/$form"}
        $base=Read-Asset 'assets/cbc_at/models/block/cluster_block.json'
        if($form -ne 'cluster') { $child=Read-Asset "assets/cbc_at/models/block/$path.json"; foreach($t in $child.textures.psobject.Properties){$base.textures|Add-Member -MemberType NoteProperty -Name $t.Name -Value $t.Value -Force} }
        $base.textures|Add-Member -NotePropertyName packing -NotePropertyValue 'createbigcannons:item/nitropowder'
        $base.textures|Add-Member -NotePropertyName separator -NotePropertyValue 'minecraft:block/white_terracotta'
        $shell=$base.elements[0]
        foreach($f in @('north','south','east','west')) {$shell.faces.psobject.Properties.Remove($f)}
        $body=@($shell)
        foreach($f in @('north','south','east','west')) {
            $body+=Quad $f 3 0 13 8 '#1' @(3,8,13,16)
            $body+=Quad $f 3 13.6 13 16 '#1' @(3,0,13,2.4)
        }
        $base.elements=$body
        # Placed blocks use the same complete flat item mesh through their existing BE renderer.
        $world=$base|ConvertTo-Json -Depth 35|ConvertFrom-Json
        $world.elements=@()
        Write-Asset "assets/cbc_at/models/block/$path.json" $world
        $overrides=@();$index=0
        foreach($variant in @('empty','he','hef','heat','smoke')) {
            $model=$base|ConvertTo-Json -Depth 35|ConvertFrom-Json
            if($variant -ne 'empty') {
                $ammo=Read-Asset "assets/cbc_at/models/item/ha_${variant}_item.json"
                $model.textures|Add-Member -NotePropertyName payload -NotePropertyValue $ammo.textures.'0'
            }
            foreach($f in @('north','south','east','west')) {
                if($variant -eq 'empty') { $model.elements+=Quad $f 3 8 13 13.6 '#packing' @(7.5,8.5,7.5,8.5); continue }
                # Three equal payload strips fill only the existing body band; shell UVs stay untouched.
                foreach($interval in @(@(3,6.333333),@(6.333333,9.666667),@(9.666667,13))) {
                    $model.elements+=Quad $f $interval[0] 8 $interval[1] 13.6 '#payload' $ammo.elements[0].faces.$f.uv
                }
            }
            Write-Asset "assets/cbcatfix/models/item/${form}_${variant}.json" $model
            if($variant -ne 'empty') {$index++; $overrides+=@{predicate=@{'cbcatfix:cluster_payload'=$index};model="cbcatfix:item/${form}_${variant}"}}
        }
        Write-Asset "assets/cbc_at/models/item/$form.json" @{parent="cbcatfix:item/${form}_empty";overrides=$overrides}
    }
} finally {$arc.Dispose()}